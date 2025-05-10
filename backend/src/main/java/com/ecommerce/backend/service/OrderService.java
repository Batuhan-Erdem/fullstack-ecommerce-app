package com.ecommerce.backend.service;

import com.ecommerce.backend.exception.MissingAddressException;
import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;

    private void validateUserAddress(User user) {
        if (user.getAddresses().isEmpty()) {
            throw new MissingAddressException("The user does not have an address. Add address before ordering.");
        }
    }

    public Order placeOrder(Long userId, List<Long> productIds, List<Integer> quantities) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateUserAddress(customer);
        Address latestAddress = customer.getAddresses().get(0);
        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        Order order = Order.builder()
                .customer(customer)
                .address(latestAddress)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PREPARING)
                .build();

        for (int i = 0; i < productIds.size(); i++) {
            Product product = productRepository.findById(productIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            int quantity = quantities.get(i);

            if (product.getStock() < quantity) {
                throw new RuntimeException("Insufficient stock: " + product.getName());
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .priceAtPurchase(product.getPrice().doubleValue())
                    .status(OrderItemStatus.PREPARING)
                    .order(order) // ✅ BURADA SET EDİLİYOR
                    .build();

            items.add(item);
            total += quantity * product.getPrice().doubleValue();
        }

        order.setItems(items);
        order.setTotalPrice(total);
        return orderRepository.save(order);
    }

    @Transactional
    public Order placeOrderFromCart(Long userId) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateUserAddress(customer);

        Cart cart = cartRepository.findByUser(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found or empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        Address latestAddress = customer.getAddresses().get(0);
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;

        Order order = Order.builder()
                .customer(customer)
                .address(latestAddress)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PREPARING)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            // 🔧 BURASI ÖNEMLİ — product'ı DB'den yeniden çekiyoruz
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice().doubleValue())
                    .status(OrderItemStatus.PREPARING)
                    .order(order)
                    .build();

            orderItems.add(item);
            total += cartItem.getQuantity() * product.getPrice().doubleValue();
        }

        order.setItems(orderItems);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public void updateOrderItemStatus(Long orderItemId, OrderItemStatus status) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        orderItem.setStatus(status);
        orderItemRepository.save(orderItem);

        // ✅ Order'ı da güncelle
        Order order = orderItem.getOrder();
        boolean allShipped = order.getItems().stream().allMatch(item -> item.getStatus() == OrderItemStatus.SHIPPED);
        boolean allDelivered = order.getItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.DELIVERED);

        if (allDelivered) {
            order.setStatus(OrderStatus.DELIVERED);
        } else if (allShipped) {
            order.setStatus(OrderStatus.SHIPPED);
        } else {
            order.setStatus(OrderStatus.PREPARING);
        }

        orderRepository.save(order);
    }

    public void updateOrderStatus(Long orderId, Long sellerId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        for (OrderItem item : order.getItems()) {
            if (!item.getProduct().getSeller().getId().equals(sellerId)) {
                throw new RuntimeException("Unauthorized: Bu ürünü güncelleme yetkiniz yok.");
            }
        }

        if (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.DELIVERED
                || newStatus == OrderStatus.PREPARING) {
            order.setStatus(newStatus);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Invalid status change by seller");
        }
    }

    public void cancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            product = productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public List<Order> getOrdersByCustomer(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return orderRepository.findByCustomer(user);
    }

    public double getOrderTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return order.getTotalPrice();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    // Kullanıcı değişim talebinde bulunur
    public void requestExchange(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Bu sipariş size ait değil");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Yalnızca teslim edilen siparişler için değişim talep edilebilir.");
        }

        order.setStatus(OrderStatus.EXCHANGE_REQUESTED);
        orderRepository.save(order);
    }

    // Satıcı değişim talebini onaylar
    public void approveExchangeRequest(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        for (OrderItem item : order.getItems()) {
            if (!item.getProduct().getSeller().getId().equals(sellerId)) {
                throw new RuntimeException("Bu sipariş ürünleri sizin değil.");
            }
        }

        if (order.getStatus() != OrderStatus.EXCHANGE_REQUESTED) {
            throw new RuntimeException("Sipariş değişim beklemiyor.");
        }

        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);
    }

}
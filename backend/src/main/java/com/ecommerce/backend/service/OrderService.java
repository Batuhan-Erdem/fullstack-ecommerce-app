package com.ecommerce.backend.service;

import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    public Order placeOrder(Long userId, List<Long> productIds, List<Integer> quantities) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        for (int i = 0; i < productIds.size(); i++) {
            Product product = productRepository.findById(productIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            int quantity = quantities.get(i);

            if (product.getStock() < quantity) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .priceAtPurchase(product.getPrice().doubleValue())
                    .build();

            items.add(item);
            total += quantity * product.getPrice().doubleValue();
        }

        Order order = Order.builder()
                .customer(customer)
                .items(items)
                .totalPrice(total)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PREPARING) // ödeme başarılı, sipariş hazırlanıyor
                .build();

        return orderRepository.save(order);
    }

    public Order placeOrderFromCart(Long userId) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found or empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Sepet boş");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Yetersiz stok: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice().doubleValue())
                    .build();

            orderItems.add(item);
            total += cartItem.getQuantity() * product.getPrice().doubleValue();
        }

        Order order = Order.builder()
                .customer(customer)
                .items(orderItems)
                .totalPrice(total)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PREPARING)
                .build();

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public void updateOrderStatus(Long orderId, Long sellerId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        for (OrderItem item : order.getItems()) {
            if (!item.getProduct().getSeller().getId().equals(sellerId)) {
                throw new RuntimeException("Unauthorized: Bu ürünü güncelleme yetkiniz yok.");
            }
        }

        // sadece SHIPPED ve DELIVERED yapılmasına izin veriyoruz
        if (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.PREPARING) {
            order.setStatus(newStatus);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Invalid status change by seller");
        }
    }

    public void cancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // stok geri eklenir
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
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
}

package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItemStatus;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.StripeService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final UserRepository userRepository;

    private final OrderService orderService;
    private final StripeService stripeService;

    // 🛒 Sepetten sipariş oluştur
    @PostMapping("/from-cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> placeOrderFromCart(@RequestParam Long userId, Principal principal) {
        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Kendi siparişi mi kontrolü
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to place this order.");
        }

        try {
            Order order = orderService.placeOrderFromCart(userId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order creation failed: " + e.getMessage());
        }
    }

    // 🔃 Sipariş durumunu güncelle (Sadece SELLER -> PREPARING → SHIPPED →
    // DELIVERED)
    @PutMapping("/update-status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<String> updateStatus(
            @RequestParam Long orderId,
            @RequestParam Long sellerId,
            @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(orderId, sellerId, status);
        return ResponseEntity.ok("Durum güncellendi: " + status.name());
    }

    @PutMapping("/update-item-status")
    public ResponseEntity<String> updateOrderItemStatus(
            @RequestParam Long orderItemId,
            @RequestParam OrderItemStatus status) {
        orderService.updateOrderItemStatus(orderItemId, status);
        return ResponseEntity.ok("Order item status updated to " + status.name());
    }

    // ❌ Siparişi Admin iptal eder
    @PutMapping("/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cancelOrderByAdmin(@RequestParam Long orderId) {
        orderService.cancelOrderByAdmin(orderId);
        return ResponseEntity.ok("Sipariş iptal edildi");
    }

    // 💸 Admin ödeme iadesi yapar
    @PutMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> refundOrder(@RequestParam Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);

            if (order.getPaymentIntentId() == null || order.getPaymentIntentId().isBlank()) {
                return ResponseEntity.badRequest().body("Bu sipariş için ödeme bilgisi bulunamadı.");
            }

            stripeService.refundPayment(order.getPaymentIntentId());
            order.setStatus(OrderStatus.CANCELLED);
            orderService.saveOrder(order);

            return ResponseEntity.ok("İade işlemi başarıyla gerçekleşti.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("İade işlemi sırasında hata: " + e.getMessage());
        }
    }

    @GetMapping("/by-customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrders(@RequestParam Long userId, Principal principal) {
        try {
            String email = principal.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!currentUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not allowed to view orders of another user.");
            }

            return ResponseEntity.ok(orderService.getOrdersByCustomer(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to fetch orders: " + e.getMessage());
        }
    }

    // Sipariş detaylarını getir
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SELLER')")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // Kullanıcı değişim talebi oluşturur
    @PutMapping("/request-exchange")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> requestExchange(
            @RequestParam Long orderId,
            @RequestParam Long userId) {
        orderService.requestExchange(orderId, userId);
        return ResponseEntity.ok("Değişim talebiniz alınmıştır.");
    }

    // Satıcı değişimi onaylar
    @PutMapping("/approve-exchange")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<String> approveExchangeRequest(
            @RequestParam Long orderId,
            @RequestParam Long sellerId) {
        orderService.approveExchangeRequest(orderId, sellerId);
        return ResponseEntity.ok("Değişim onaylandı, sipariş tekrar hazırlanıyor.");
    }

}
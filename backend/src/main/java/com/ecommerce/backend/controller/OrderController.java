package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StripeService stripeService;

    // 🛒 Sepetten sipariş oluştur
    @PostMapping("/from-cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> placeOrderFromCart(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.placeOrderFromCart(userId));
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

    // 📦 Kullanıcının tüm siparişlerini getir
    @GetMapping("/by-customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Order>> getOrders(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(userId));
    }

    // 📄 Sipariş detaylarını getir
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SELLER')")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // 📦 Kullanıcı değişim talebi oluşturur
    @PutMapping("/request-exchange")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> requestExchange(
            @RequestParam Long orderId,
            @RequestParam Long userId) {
        orderService.requestExchange(orderId, userId);
        return ResponseEntity.ok("Değişim talebiniz alınmıştır.");
    }

    // ✔️ Satıcı değişimi onaylar
    @PutMapping("/approve-exchange")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<String> approveExchangeRequest(
            @RequestParam Long orderId,
            @RequestParam Long sellerId) {
        orderService.approveExchangeRequest(orderId, sellerId);
        return ResponseEntity.ok("Değişim onaylandı, sipariş tekrar hazırlanıyor.");
    }

}

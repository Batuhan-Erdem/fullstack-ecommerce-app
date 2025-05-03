package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.service.OrderService;
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

    // 🛒 Sepetten sipariş oluştur
    @PostMapping("/from-cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> placeOrderFromCart(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.placeOrderFromCart(userId));
    }

    // 🔃 Sipariş durumunu güncelle (Sadece SELLER -> PREPARING → SHIPPED → DELIVERED)
    @PutMapping("/update-status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<String> updateStatus(
            @RequestParam Long orderId,
            @RequestParam Long sellerId,
            @RequestParam OrderStatus status
    ) {
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
}

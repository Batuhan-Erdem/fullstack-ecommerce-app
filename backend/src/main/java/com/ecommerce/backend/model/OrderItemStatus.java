package com.ecommerce.backend.model;

public enum OrderItemStatus {
    PREPARING,
    PENDING,        // Henüz gönderilmedi
    SHIPPED,        // Gönderildi
    DELIVERED,      // Teslim edildi
    EXCHANGED,      // Değiştirildi
    CANCELLED       // İptal edildi
}

package com.ecommerce.backend.model;

public enum OrderItemStatus {
    PREPARING,
    SHIPPED,        // Gönderildi
    DELIVERED,      // Teslim edildi
    EXCHANGED,      // Değiştirildi
    CANCELLED       // İptal edildi
}

package com.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@ManyToOne(cascade = CascadeType.REMOVE)
@JoinColumn(name = "product_id")
@JsonIgnoreProperties({"category", "seller"})
    private Product product;


@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "cart_id")
@JsonIgnoreProperties({"user", "items"})
    private Cart cart; // ✅ Hangi sepete ait olduğunu tutar

    private int quantity;
}

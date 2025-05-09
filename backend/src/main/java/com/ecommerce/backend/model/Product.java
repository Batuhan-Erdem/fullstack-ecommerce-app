package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private BigDecimal price;

    private int stock;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>(); // image URL'leri

    @ManyToOne
    @JsonBackReference() // ✅ Sonsuz döngüyü önler
    private Category category;

    @ManyToOne
    @JsonBackReference("seller-products")
    @JoinColumn(name = "seller_id")
    private User seller;

    private boolean active = true;

    private boolean deletedByAdmin = false;
}

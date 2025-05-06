package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }
    @GetMapping("/active-not-deleted")
    public ResponseEntity<List<Product>> getActiveAndNotDeletedProducts() {
        List<Product> products = productService.getActiveAndNotDeletedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductsByCategory(id));
    }

    @PostMapping("/add/{sellerId}/{categoryId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> addProduct(
            @RequestBody Product product,
            @PathVariable Long sellerId,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(productService.addProduct(product, sellerId, categoryId));
    }

    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProductByAdmin(id);
        return ResponseEntity.ok().build();
    }
}

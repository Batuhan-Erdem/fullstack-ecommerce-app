package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findBySeller(User seller);
    List<Product> findByActiveTrue(); 
    List<Product> findByActiveTrueAndDeletedByAdminFalse();
}

package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Buraya özel sorgular ekleyebilirsiniz
}

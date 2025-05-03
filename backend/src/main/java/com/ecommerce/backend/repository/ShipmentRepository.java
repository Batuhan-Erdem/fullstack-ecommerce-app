package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}

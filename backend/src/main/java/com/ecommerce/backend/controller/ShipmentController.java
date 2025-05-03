package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Shipment;
import com.ecommerce.backend.model.ShipmentStatus;
import com.ecommerce.backend.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Shipment> createShipment(
            @RequestParam Long orderId,
            @RequestParam String trackingNumber
    ) {
        Shipment shipment = shipmentService.createShipment(orderId, trackingNumber);
        return ResponseEntity.ok(shipment);
    }

    @PutMapping("/update/{shipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long shipmentId,
            @RequestParam ShipmentStatus status
    ) {
        shipmentService.updateShipmentStatus(shipmentId, status);
        return ResponseEntity.ok().build();
    }
}

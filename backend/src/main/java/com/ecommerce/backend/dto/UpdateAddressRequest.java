package com.ecommerce.backend.dto;

import lombok.Data;

@Data
public class UpdateAddressRequest {

    private Long addressId; // Address ID'si ekliyoruz.
    private String addressLine;
    private String city;
    private String postalCode;
    private String country;
    private String phoneNumber;
}

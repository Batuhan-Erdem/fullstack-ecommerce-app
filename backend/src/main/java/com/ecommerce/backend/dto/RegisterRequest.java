package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}

  
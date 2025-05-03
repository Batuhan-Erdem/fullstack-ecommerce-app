package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private Role role;
}

  
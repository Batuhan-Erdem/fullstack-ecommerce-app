package com.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingAddressException.class)
    public ResponseEntity<String> handleMissingAddress(MissingAddressException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ " + e.getMessage());
    }

    // Diğer exceptionlar da buraya eklenebilir
}

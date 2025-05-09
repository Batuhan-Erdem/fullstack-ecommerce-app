package com.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.stripe.exception.ApiException;

@ControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(ApiException.class)
public ResponseEntity<String> handleApiException(ApiException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ " + e.getMessage());
}
    @ExceptionHandler(MissingAddressException.class)
    public ResponseEntity<?> handleMissingAddress(MissingAddressException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}

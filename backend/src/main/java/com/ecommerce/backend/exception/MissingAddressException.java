package com.ecommerce.backend.exception;

public class MissingAddressException extends RuntimeException {
    public MissingAddressException(String message) {
        super(message);
    }
}

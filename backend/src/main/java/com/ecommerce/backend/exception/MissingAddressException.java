package com.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 hatası döndürür
public class MissingAddressException extends RuntimeException {
    public MissingAddressException(String message) {
        super(message);
    }
}

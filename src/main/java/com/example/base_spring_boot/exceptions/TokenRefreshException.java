package com.example.base_spring_boot.exceptions;

public class TokenRefreshException
        extends RuntimeException {

    public TokenRefreshException(String message) {
        super(message);
    }
}
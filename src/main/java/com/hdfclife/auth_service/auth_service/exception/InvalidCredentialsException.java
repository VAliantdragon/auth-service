package com.hdfclife.auth_service.auth_service.exception;
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) { super(message); }
}
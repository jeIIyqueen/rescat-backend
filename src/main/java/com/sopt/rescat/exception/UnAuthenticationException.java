package com.sopt.rescat.exception;

public class UnAuthenticationException extends RuntimeException {
    public UnAuthenticationException() {
    }

    public UnAuthenticationException(String message) {
        super(message);
    }
}

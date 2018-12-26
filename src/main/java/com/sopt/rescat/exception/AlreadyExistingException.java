package com.sopt.rescat.exception;

public class AlreadyExistingException extends RuntimeException {
    public AlreadyExistingException() {
    }

    public AlreadyExistingException(String message) {
        super(message);
    }
}

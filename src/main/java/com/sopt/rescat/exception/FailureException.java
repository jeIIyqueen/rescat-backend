package com.sopt.rescat.exception;

public class FailureException extends RuntimeException {
    public FailureException() {

    }

    public FailureException(String message) {
        super(message);
    }
}

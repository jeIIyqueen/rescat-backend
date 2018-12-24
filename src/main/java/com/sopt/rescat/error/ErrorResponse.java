package com.sopt.rescat.error;

public class ErrorResponse {
    private String message;

    private ErrorResponse(String message) {
        this.message = message;
    }

    public static ErrorResponse ofString(String message) {
        return new ErrorResponse(message);
    }
}

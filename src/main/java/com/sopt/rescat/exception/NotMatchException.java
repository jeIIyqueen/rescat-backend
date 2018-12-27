package com.sopt.rescat.exception;

import lombok.Getter;

@Getter
public class NotMatchException extends RuntimeException {
    private String field;

    public NotMatchException() {

    }

    public NotMatchException(String field, String message) {
        super(message);
        this.field = field;
    }
}

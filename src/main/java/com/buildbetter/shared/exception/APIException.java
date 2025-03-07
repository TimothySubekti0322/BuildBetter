package com.buildbetter.shared.exception;

public class APIException extends RuntimeException {
    public APIException(String message) {
        super(message);
    }
}

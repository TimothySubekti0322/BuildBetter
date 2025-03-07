package com.buildbetter.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final int code;
    private final String name;

    public BadRequestException(String message) {
        super(message);
        this.code = HttpStatus.BAD_REQUEST.value();
        this.name = HttpStatus.BAD_REQUEST.name();
    }
}

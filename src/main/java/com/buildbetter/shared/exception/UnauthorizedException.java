package com.buildbetter.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final int code;
    private final String name;

    public UnauthorizedException(String message) {
        super(message);
        this.code = HttpStatus.UNAUTHORIZED.value();
        this.name = HttpStatus.UNAUTHORIZED.name();
    }
}

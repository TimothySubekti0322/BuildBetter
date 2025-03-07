package com.buildbetter.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final int code;
    private final String name;

    public ForbiddenException(String message) {
        super(message);
        this.code = HttpStatus.FORBIDDEN.value();
        this.name = HttpStatus.FORBIDDEN.name();
    }

}

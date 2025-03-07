package com.buildbetter.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final int code;
    private final String name;

    public NotFoundException(String message) {
        super(message);
        this.code = HttpStatus.NOT_FOUND.value();
        this.name = HttpStatus.NOT_FOUND.name();
    }

}

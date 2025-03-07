package com.buildbetter.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class InternalServerErrorException extends RuntimeException {

    private final int code;
    private final String name;

    public InternalServerErrorException(String message) {
        super(message);
        this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.name = HttpStatus.INTERNAL_SERVER_ERROR.name();
    }

}

package com.buildbetter.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class TooManyRequestException extends RuntimeException {

    private final int code;
    private final String name;

    public TooManyRequestException(String message) {
        super(message);
        this.code = HttpStatus.TOO_MANY_REQUESTS.value();
        this.name = HttpStatus.TOO_MANY_REQUESTS.name();
    }

}

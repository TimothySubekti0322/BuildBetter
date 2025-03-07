package com.buildbetter.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.buildbetter.shared.dto.ErrorResponse;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.ForbiddenException;
import com.buildbetter.shared.exception.InternalServerErrorException;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.exception.UnauthorizedException;

@RestControllerAdvice(basePackages = "com.buildbetter.user.controller")
public class UserExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ErrorResponse handleBadRequest(BadRequestException exception) {
        return new ErrorResponse(exception.getCode(), exception.getName(), exception.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ErrorResponse handleForbidden(ForbiddenException ex) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ex.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ErrorResponse handleInternalServerError(InternalServerErrorException ex) {
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(),
                ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ErrorResponse handleUnauthorized(UnauthorizedException ex) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(), ex.getMessage());
    }
}

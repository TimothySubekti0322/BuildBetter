package com.buildbetter.shared.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.buildbetter.shared.dto.ErrorResponse;

@RestControllerAdvice(basePackages = {
                "com.buildbetter.user",
                "com.buildbetter.plan",
                "com.buildbetter.consultation",
                "com.buildbetter.article",
})
public class ApiExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse<List<String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {
                List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .toList();

                ErrorResponse<List<String>> errorResponse = new ErrorResponse<>(HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.name(),
                                errors);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse<String>> handleBadRequest(BadRequestException exception) {
                ErrorResponse<String> errorResponse = new ErrorResponse<>(exception.getCode(), exception.getName(),
                                exception.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse<String>> handleForbidden(ForbiddenException exception) {
                ErrorResponse<String> errorResponse = new ErrorResponse<>(exception.getCode(), exception.getName(),
                                exception.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(InternalServerErrorException.class)
        public ResponseEntity<ErrorResponse<String>> handleInternalServerError(InternalServerErrorException exception) {
                ErrorResponse<String> errorResponse = new ErrorResponse<>(exception.getCode(), exception.getName(),
                                exception.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse<String>> handleNotFound(NotFoundException exception) {
                ErrorResponse<String> errorResponse = new ErrorResponse<>(exception.getCode(), exception.getName(),
                                exception.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(TooManyRequestException.class)
        public ResponseEntity<ErrorResponse<String>> handleTooManyRequests(TooManyRequestException exception) {
                ErrorResponse<String> errorResponse = new ErrorResponse<>(exception.getCode(), exception.getName(),
                                exception.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse<String>> handleUnauthorized(UnauthorizedException exception) {
                ErrorResponse<String> errorResponse = new ErrorResponse<>(exception.getCode(), exception.getName(),
                                exception.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
}

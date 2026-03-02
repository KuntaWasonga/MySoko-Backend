package com.dukani.apigateway.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomJwtException extends RuntimeException {
    private final HttpStatus status;

    public CustomJwtException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}

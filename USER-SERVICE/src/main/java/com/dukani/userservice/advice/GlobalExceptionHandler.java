package com.dukani.userservice.advice;

import com.dukani.userservice.dtos.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;


    // ERROR 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardResponse<?>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        log.error("BadCredentialsException: ", ex);

        var status = HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(
                StandardResponse.builder()
                        .message(ex.getMessage())
                        .status(status.value())
                        .timestamp(LocalDateTime.now(clock))
                        .path(request.getRequestURI())
                        .build(), status);
    }

    // ERROR 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleGlobalException(Exception ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled exception [Error ID: {}] for request: {} {}",
                errorId, request.getMethod(), request.getRequestURI(), ex);

        var status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(
                StandardResponse.builder()
                        .message("An unexpected error occurred")
                        .status(status.value())
                        .timestamp(LocalDateTime.now(clock))
                        .path(request.getRequestURI())
                        .build(), status);
    }
}

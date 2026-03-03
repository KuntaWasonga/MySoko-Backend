package com.dukani.productservice.advice;

import com.dukani.productservice.dtos.StandardResponse;
import com.dukani.productservice.exceptions.ItemNotFoundException;
import com.dukani.productservice.infrastructure.OracleConstraintResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final OracleConstraintResolver constraintResolver;
    private final Clock clock;


    // ERROR 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        var status = HttpStatus.BAD_REQUEST;

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return new ResponseEntity<>(
                StandardResponse.builder()
                        .message("Validation failed")
                        .data(errors)
                        .status(status.value())
                        .timestamp(LocalDateTime.now(clock))
                        .path(request.getRequestURI())
                        .build(), status);
    }

    // ERROR 404
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<StandardResponse<?>> handleItemNotFoundException(
            ItemNotFoundException ex, HttpServletRequest request) {
        log.error("ItemNotFoundException: ", ex);

        var status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(
                StandardResponse.builder()
                        .message(ex.getMessage())
                        .status(status.value())
                        .timestamp(LocalDateTime.now(clock))
                        .path(request.getRequestURI())
                        .build(), status);
    }

    // ERROR 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardResponse<?>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("DataIntegrityViolationException: ", ex);

        var status = HttpStatus.CONFLICT;
        String message = constraintResolver.resolve(ex);

        return new ResponseEntity<>(
                StandardResponse.builder()
                        .message(message)
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

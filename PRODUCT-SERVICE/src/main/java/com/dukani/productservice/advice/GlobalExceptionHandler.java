package com.dukani.productservice.advice;

import com.dukani.productservice.dtos.StandardResponse;
import com.dukani.productservice.exceptions.ItemNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;


@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ERROR 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("IllegalArgumentException: ", ex);

        var status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<StandardResponse<?>>(
                StandardResponse.builder()
                        .message(ex.getMessage())
                        .status(status.value())
                        .timestamp(LocalDateTime.now())
                        .path(request instanceof ServletWebRequest ? ((ServletWebRequest) request).getRequest().getRequestURI() : null)
                        .build(), status);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<?> handleItemNotFoundException(ItemNotFoundException ex, WebRequest request) {
        log.error("ItemNotFoundException: ", ex);

        var status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<StandardResponse<?>>(
                StandardResponse.builder()
                        .message(ex.getMessage())
                        .status(status.value())
                        .timestamp(LocalDateTime.now())
                        .path(request instanceof ServletWebRequest ? ((ServletWebRequest) request).getRequest().getRequestURI() : null)
                        .build(), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Exception: ", ex);

        var status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(
                StandardResponse.builder()
                        .message("An unexpected error occurred")
                        .status(status.value())
                        .timestamp(LocalDateTime.now())
                        .path(request instanceof ServletWebRequest ? ((ServletWebRequest) request).getRequest().getRequestURI() : null)
                        .build(), status);
    }
}

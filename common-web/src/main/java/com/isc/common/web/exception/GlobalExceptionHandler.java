package com.isc.common.web.exception;

import com.isc.common.exception.BusinessException;
import com.isc.common.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex) {

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        Instant.now(),
                        ex.getCode(),
                        ex.getMessage()
                )
        );
    }
}

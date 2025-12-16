package com.contracthawk.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<FieldErrorResponse> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();
                    String message = violation.getMessage();
                    return new FieldErrorResponse(field, message);
                })
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("NOT_FOUND")
                .message(ex.getMessage())
                .fieldErrors(new ArrayList<>())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .fieldErrors(new ArrayList<>())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .traceId(traceId)
                .fieldErrors(new ArrayList<>())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private String code;
        private String message;
        private String traceId;
        private List<FieldErrorResponse> fieldErrors;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FieldErrorResponse {
        private String field;
        private String message;
    }
}


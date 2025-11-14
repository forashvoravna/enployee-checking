package com.example.employeecheckingplatform.exception;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(NotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Map.of("timestamp", Instant.now().toString(), "status", 404, "error", "Not Found", "message", e.getMessage())
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String,Object>> handleBusiness(BusinessException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of("timestamp", Instant.now().toString(), "status", 400, "error", "Bad Request", "message", e.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleOther(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of("timestamp", Instant.now().toString(), "status", 500, "error", "Internal Server Error", "message", e.getMessage())
        );
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleMethodArgInvalid(org.springframework.web.bind.MethodArgumentNotValidException e){
        var fields = e.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage,
                        (a,b)->a
                ));
        return ResponseEntity.badRequest().body(
                Map.of("timestamp", java.time.Instant.now().toString(),
                        "status", 400, "error","Bad Request",
                        "message","Validation failed","fields", fields)
        );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String,Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException e){
        return ResponseEntity.status(403).body(
                Map.of("timestamp", java.time.Instant.now().toString(), "status", 403,
                        "error","Forbidden","message", e.getMessage())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,Object>> handleConstraint(ConstraintViolationException e){
        var fields = e.getConstraintViolations().stream().collect(java.util.stream.Collectors.toMap(
                v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage, (a, b)->a
        ));
        return ResponseEntity.badRequest().body(
                Map.of("timestamp", java.time.Instant.now().toString(), "status", 400,
                        "error","Bad Request","message","Constraint violated","fields", fields)
        );
    }

}

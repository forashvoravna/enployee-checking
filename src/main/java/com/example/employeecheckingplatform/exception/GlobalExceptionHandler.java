package com.example.employeecheckingplatform.exception;
import com.example.employeecheckingplatform.component.ApiResponse;
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
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException e){
        // status: false, message: e.getMessage(), data: null
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(), false));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(e.getMessage(), false));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("Sizda ushbu amalni bajarish uchun ruxsat yo'q: " + e.getMessage(), false));
    }

    // Validatsiya xatolari uchun (Field xatolarini ham message ichiga yoki data ichiga yuborish mumkin)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgInvalid(org.springframework.web.bind.MethodArgumentNotValidException e){
        Map<String, String> fields = e.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Xato",
                        (a, b) -> a
                ));

        // Bu yerda xatolar ro'yxatini data ichida yuboramiz
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>("Validatsiya xatoligi" + e.getMessage(), false, fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraint(ConstraintViolationException e){
        Map<String, String> fields = e.getConstraintViolations().stream().collect(java.util.stream.Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (a, b) -> a
        ));
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>("Cheklov buzildi (Constraint violation)" + e.getMessage(), false, fields));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOther(Exception e){
        // Logika ichidagi kutilmagan xatolar
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Serverda kutilmagan xatolik yuz berdi" + e.getMessage()    , false));
    }
}
package com.example.employeecheckingplatform.component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    String message;
    boolean status;
    T data;

    // Faqat xabar va status qaytarish uchun (masalan, Delete amalida)
    public ApiResponse(String message, boolean status) {
        this.message = message;
        this.status = status;
    }
}
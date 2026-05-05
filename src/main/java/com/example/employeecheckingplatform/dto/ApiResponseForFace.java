package com.example.employeecheckingplatform.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponseForFace {

    String message;
    String status;
    boolean success;
    Object data;

    public ApiResponseForFace(String message, String status, boolean success){
        this.message = message;
        this.status = status;
        this.success = success;
    }
}

package com.example.employeecheckingplatform.dto.sec;

import com.example.employeecheckingplatform.entity.UserRole;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        String middleName,
        String jshshir,
        UserRole role,
        Long branchId,
        Long rankId,
        String gender
) {}
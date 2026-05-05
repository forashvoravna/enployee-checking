package com.example.employeecheckingplatform.dto.user;

import com.example.employeecheckingplatform.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Foydalanuvchi yaratish uchun ma'lumotlar tashuvchi obyekt (DTO).
 */
public record UserCreateDto(

        @NotBlank(message = "Username bo'sh bo'lmasligi kerak")
        @Size(min = 3, max = 50, message = "Username 3 va 50 belgi oralig'ida bo'lishi kerak")
        String username,

        @NotBlank(message = "Parol bo'sh bo'lmasligi kerak")
        @Size(min = 6, message = "Parol kamida 6 ta belgidan iborat bo'lishi kerak")
        String password,

        @NotBlank(message = "Ism kiritilishi shart")
        String firstName,

        @NotBlank(message = "Familiya kiritilishi shart")
        String lastName,

        String middleName,

        @NotBlank(message = "Jinsni belgilash shart")
        String gender,

        @NotBlank(message = "JSHSHIR kiritilishi shart")
        @Pattern(regexp = "\\d{14}", message = "JSHSHIR 14 ta raqamdan iborat bo'lishi kerak")
        String jshshir,

        UserRole userRole, // "ADMIN", "USER", "MODERATOR"

        @NotNull(message = "Filial (Branch) ID kiritilishi shart")
        Long branchId,

        @NotNull(message = "Unvon (Rank) ID kiritilishi shart")
        Long rankId

) {}
package com.example.employeecheckingplatform.dto.user;

import com.example.employeecheckingplatform.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserUpdateDto(
        @NotBlank(message = "Ism bo'sh bo'lmasligi kerak")
        String firstName,

        @NotBlank(message = "Familiya bo'sh bo'lmasligi kerak")
        String lastName,

        String middleName,

        @NotBlank(message = "Username bo'sh bo'lmasligi kerak")
        String username,

        @Pattern(regexp = "\\d{14}", message = "JSHSHIR 14 ta raqamdan iborat bo'lishi kerak")
        String jshshir,

        @NotNull(message = "Rol tanlanishi shart")
        UserRole userRole,

        @NotNull(message = "Filial ID majburiy")
        Long branchId,

        @NotNull(message = "Unvon (Rank) ID majburiy")
        Long rankId,

        String gender

) {
}
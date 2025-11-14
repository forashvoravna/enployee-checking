package com.example.employeecheckingplatform.dto.foydalanuvchi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FoydalanuvchiUpdateDto(
        @NotBlank String toliqIsm,
        @NotBlank String username,

        String roli,
        @NotNull Boolean faol
) {
}

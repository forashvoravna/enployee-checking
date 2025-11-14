package com.example.employeecheckingplatform.dto.foydalanuvchi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FoydalanuvchiCreateDto(
        @NotBlank @Size(max = 150) String toliqIsm,
        @NotBlank @Size(max = 150) String username,   // JShShIR
        String parol,                               // ixtiyoriy (FaceID bo'lsa bo'sh)
        @NotBlank String roli,                         // "ADMIN" yoki "USER"
        @NotNull Long tashkilotId                      // 🔗 majburiy
) {}
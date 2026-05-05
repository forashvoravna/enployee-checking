package com.example.employeecheckingplatform.dto.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Bo'linma (Unit) yaratish uchun ma'lumotlar tashuvchi obyekt.
 */
public record UnitTypeCreateDto(
        String nomi,
        Long original_id,
        Long darajasi
) {}
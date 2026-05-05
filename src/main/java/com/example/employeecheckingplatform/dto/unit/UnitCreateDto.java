package com.example.employeecheckingplatform.dto.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Bo'linma (Unit) yaratish uchun ma'lumotlar tashuvchi obyekt.
 */
public record UnitCreateDto(
        @NotBlank(message = "Bo'linma nomi bo'sh bo'lmasligi kerak")
        @Size(max = 255, message = "Nomi juda uzun")
        String nomi,

        Long parentId, // Agar ildiz bo'linma bo'lsa, null bo'lishi mumkin

        @NotNull(message = "Bo'linma turi (Branch Type ID) kiritilishi shart")
        Long branchTypeId
) {}
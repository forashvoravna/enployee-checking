// Yaratish uchun DTO
package com.example.employeecheckingplatform.dto.imtihon;

import jakarta.validation.constraints.*;

import java.util.Set;

public record ImtihonCreateDto(
        @NotNull Long fanId,
        @NotBlank String nomi,
        @NotNull @Min(1) Integer davomiylikDaqiqa,
        @NotNull @Min(1) Integer maxUrinish,
        @Min(1) Integer savolSoni,
        @NotNull @Min(0) @Max(100) Integer aloPct,
        @NotNull @Min(0) @Max(100) Integer yaxshiPct,
        @NotNull @Min(0) @Max(100) Integer qoniqarliPct
) {}

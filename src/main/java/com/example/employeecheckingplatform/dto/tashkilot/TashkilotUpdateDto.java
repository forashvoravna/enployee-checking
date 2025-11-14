package com.example.employeecheckingplatform.dto.tashkilot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TashkilotUpdateDto(@NotBlank String nomi, Long parentId, @NotBlank String turi) {}

package com.example.employeecheckingplatform.dto.variant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VariantCreateDto(@NotBlank String matn, @NotNull Boolean togri) {}

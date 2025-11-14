package com.example.employeecheckingplatform.dto.savol;

import com.example.employeecheckingplatform.dto.variant.VariantCreateDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavolCreateDto(@NotNull Long fanId, @NotBlank String matn,
                             @Size(min=4, max = 4) List<VariantCreateDto> variantlar) {}
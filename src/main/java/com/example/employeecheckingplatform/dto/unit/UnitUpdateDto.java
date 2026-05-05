package com.example.employeecheckingplatform.dto.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnitUpdateDto(
        @NotBlank String nomi,
        Long parentId,
        @NotNull Long branchTypeId // 'turi' o'rniga 'branchTypeId'
) {}
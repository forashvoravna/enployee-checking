package com.example.employeecheckingplatform.dto.savol;

import com.example.employeecheckingplatform.dto.variant.VariantResponseDto;

import java.util.List;

public record SavolResponseDto(
        Long id,
        String matn,
        Long fanId,
        String fanNomi,
        List<VariantResponseDto> variantlar
) {}
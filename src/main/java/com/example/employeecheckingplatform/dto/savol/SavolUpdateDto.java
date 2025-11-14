package com.example.employeecheckingplatform.dto.savol;

import com.example.employeecheckingplatform.dto.variant.VariantDto;

import java.util.List;

public record SavolUpdateDto(
        String matn,
        Long fanId,
        List<VariantDto> variantlar
) {}
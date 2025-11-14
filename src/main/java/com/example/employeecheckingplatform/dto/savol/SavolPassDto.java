package com.example.employeecheckingplatform.dto.savol;

import com.example.employeecheckingplatform.dto.variant.VariantPassDto;

import java.util.List;
public record SavolPassDto(Long id, String matn, List<VariantPassDto> variantlar) {}

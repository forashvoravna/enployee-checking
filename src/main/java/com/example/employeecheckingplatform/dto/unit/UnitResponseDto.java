package com.example.employeecheckingplatform.dto.unit;

public record UnitResponseDto(
        Long id,
        String nomi,
        Long parentId,
        Long branchTypeId,
        Integer level
) {}
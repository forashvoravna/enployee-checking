package com.example.employeecheckingplatform.dto.tashkilot;

public record TashkilotResponseDto(
        Long id,
        String nomi,
        Long parentId,
        String parentName,
        String turi
) {}

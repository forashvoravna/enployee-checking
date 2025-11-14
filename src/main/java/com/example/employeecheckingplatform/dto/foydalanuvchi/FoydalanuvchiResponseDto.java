package com.example.employeecheckingplatform.dto.foydalanuvchi;
public record FoydalanuvchiResponseDto(
        Long id,
        String toliqIsm,
        String username,
        String roli,
        Long tashkilotId,
        String tashkilotName,
        String tashkilotTuri,
        Boolean faol
) {}
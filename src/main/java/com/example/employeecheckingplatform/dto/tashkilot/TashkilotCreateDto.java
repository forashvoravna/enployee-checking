package com.example.employeecheckingplatform.dto.tashkilot;

import jakarta.validation.constraints.NotBlank;

public record TashkilotCreateDto(@NotBlank String nomi, Long parentId, @NotBlank String turi) {
}

package com.example.employeecheckingplatform.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerDto(
            @NotNull Long savolId,
            @NotNull Long variantId
    ) {}
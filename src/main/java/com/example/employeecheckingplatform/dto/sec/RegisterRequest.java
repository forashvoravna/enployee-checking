package com.example.employeecheckingplatform.dto.sec;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String toliqIsm,
        @NotBlank String username,
        @NotBlank String parol
) {}
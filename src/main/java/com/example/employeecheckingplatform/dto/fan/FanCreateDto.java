package com.example.employeecheckingplatform.dto.fan;

import jakarta.validation.constraints.NotBlank;

public record FanCreateDto(@NotBlank String nomi, String tavsif, Boolean faol) {}

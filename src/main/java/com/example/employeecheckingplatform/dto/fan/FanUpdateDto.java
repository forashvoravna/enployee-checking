package com.example.employeecheckingplatform.dto.fan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FanUpdateDto(@NotBlank String nomi, String tavsif, @NotNull Boolean faol) {
}

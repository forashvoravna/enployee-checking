package com.example.employeecheckingplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShootingResultDto(
    @Schema(example = "30808976940020")
    String jshshir,

    @Schema(example = "AK-47")
    String weaponName,

    @Schema(example = "10")
    Integer shots,

    @Schema(example = "8")
    Integer hits,

    @Schema(example = "8")
    Integer score
) {}
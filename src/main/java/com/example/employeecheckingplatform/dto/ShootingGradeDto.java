package com.example.employeecheckingplatform.dto;

import java.time.LocalDateTime;

public record ShootingGradeDto(
        Long id,
        String weaponName,
        Integer shots,
        Integer hits,
        Integer score
) {}
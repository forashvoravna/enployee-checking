package com.example.employeecheckingplatform.dto.otishga;

import java.time.Instant;

public record GradeDto(
        Long urinishId,
        Long imtihonId,
        String imtihonNomi,
        Integer ball,
        String baho,
        Instant tugadi
) {}
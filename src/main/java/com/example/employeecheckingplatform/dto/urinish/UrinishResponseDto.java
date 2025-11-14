package com.example.employeecheckingplatform.dto.urinish;

import java.time.Instant;

public record UrinishResponseDto(
        Long id,
        Long imtihonId,
        String imtihonNomi,
        Long foydalanuvchiId,
        String foydalanuvchiUsername,
        Instant boshladi,
        Instant tugadi,
        String holati,
        Integer ball,
        String baho // "ALO","YAXSHI","QONIQARLI","QONIQARSIZ" yoki null
) {}

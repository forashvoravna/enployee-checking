package com.example.employeecheckingplatform.dto.urinish;

import com.example.employeecheckingplatform.dto.javob.JavobDto;

import java.time.Instant;
import java.util.List;

public record UrinishDetailDto(
        Long id,
        Long foydalanuvchiId,
        String foydalanuvchiIsm,
        Long imtihonId,
        String imtihonNomi,
        String holati,
        Integer ball,
        Integer savollarSoni,
        Instant boshlandi,
        Instant tugadi,
        List<JavobDto> javoblar,
        String baho
) {}

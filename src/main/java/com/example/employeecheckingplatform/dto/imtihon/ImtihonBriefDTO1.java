package com.example.employeecheckingplatform.dto.imtihon;

public record ImtihonBriefDTO1(
        Long id,
        String nomi,
        Integer davomiylikDaqiqa,
        Integer maxUrinish,
        Integer savolSoni,
        Long fanId,
        String fanNomi,

        Integer attemptsUsed,
        Integer attemptsLeft,
        Boolean canStart,
        Long urinishId
) {
}

package com.example.employeecheckingplatform.dto.imtihon;

public record ImtihonBriefDTO1(
        Long id,
        String nomi,
        Integer davomiylikDaqiqa,
        Integer maxUrinish,
        Integer savolSoni,
        Long fanId,
        String fanNomi,
        Integer tekshirishSoni,
        Integer tekshirishVaqti,
        Integer aloPct,
        Integer yaxshiPct,
        Integer qoniqarliPct,
        Integer attemptsUsed,
        Integer attemptsLeft,
        Boolean canStart,
        Long urinishId
) {
}

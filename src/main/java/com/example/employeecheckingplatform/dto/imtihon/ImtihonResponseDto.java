// Imtihon javobi (frontendga)
package com.example.employeecheckingplatform.dto.imtihon;

import com.example.employeecheckingplatform.dto.RuxsatFoydalanuvchiDto;

import java.util.List;

public record ImtihonResponseDto(
        Long id,
        String nomi,
        Integer davomiylikDaqiqa,
        Integer maxUrinish,
        Integer savolSoni,
        Integer aloPct,
        Integer yaxshiPct,
        Integer qoniqarliPct,
        Boolean faol,
        Long fanId,
        String fanNomi,
        List<RuxsatFoydalanuvchiDto> ruxsatEtilganlar
) {}

// Imtihon javobi (frontendga)
package com.example.employeecheckingplatform.dto.imtihon;

import com.example.employeecheckingplatform.dto.RuxsatUserDto;

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
        Integer tekshirishVaqti,
        Integer tekshirishSoni,
        Long fanId,
        String fanNomi,
        List<RuxsatUserDto> ruxsatEtilganlar
) {}

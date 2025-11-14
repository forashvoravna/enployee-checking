package com.example.employeecheckingplatform.dto;

public record OrgExamResultDto(
        Long imtihonId,
        String imtihonNomi,
        Long fanId,
        String fanNomi,
        Long attempts,
        Double avgScore,
        Long alo,
        Long yaxshi,
        Long qoniqarli,
        Long qoniqarsiz,
        Long distinctUsers,
        Double passRate
) {}

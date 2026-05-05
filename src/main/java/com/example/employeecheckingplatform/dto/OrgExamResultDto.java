package com.example.employeecheckingplatform.dto;

public record OrgExamResultDto(
        Long imtihonId,
        String imtihonNomi,
        Integer savolSoni,
        Long fanId,
        String fanNomi,
        Integer attempts,
        Integer alo,
        Integer yaxshi,
        Integer qoniqarli,
        Integer qoniqarsiz,
        Integer distinctUsers
) {}

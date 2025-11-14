package com.example.employeecheckingplatform.dto;

import java.util.List;

public record OrgExamSummaryDto(
        Long orgId,
        Integer totalExams,      // nechta imtihon topshirilgan (distinct)
        List<OrgExamResultDto> exams
) {}

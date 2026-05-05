package com.example.employeecheckingplatform.dto;

public record PersonRawDto(
        Long id,
        String first_name,
        String last_name,
        String middle_name,
        String jshshir,
        Long branch_id
) {}

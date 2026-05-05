package com.example.employeecheckingplatform.dto;

public record BranchDto(
        Long id,
        String nomi,
        Long branch_type_id,
        Long parent_id,
        Long boshliq_person_id,
        String path,
        Integer level
) {}

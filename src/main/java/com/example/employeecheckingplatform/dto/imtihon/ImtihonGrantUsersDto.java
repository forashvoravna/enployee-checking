package com.example.employeecheckingplatform.dto.imtihon;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record ImtihonGrantUsersDto(
        @NotNull Long imtihonId,
        @NotEmpty Set<Long> userIds
) {}

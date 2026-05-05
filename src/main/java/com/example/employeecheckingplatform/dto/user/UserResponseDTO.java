package com.example.employeecheckingplatform.dto.user;

import com.example.employeecheckingplatform.entity.UserRole;
import lombok.Builder;

@Builder

public record UserResponseDTO(
        Long id,
        String username,
        String firstName,
        String lastName,
        String middleName,
        String fullName,
        String jshshir,
        String gender,
        UserRole role,
        Long branchId,
        String branchName,
        Long rankId,
        String attachmentId
) {

}
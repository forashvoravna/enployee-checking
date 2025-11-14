// package com.example.employeecheckingplatform.dto;
package com.example.employeecheckingplatform.dto;

public record TokenResponse(
    String access_token,
    String token_type,
    Long expires_in
) {}

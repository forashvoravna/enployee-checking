package com.example.employeecheckingplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record FaceLoginBase64Request(@NotBlank String imageBase64) {}

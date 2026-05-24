package com.local.hospitalapi.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PatientRequest(
    @NotBlank(message = "name is required") String name,
    @Min(value = 0, message = "age must be >= 0") int age,
    @NotBlank(message = "gender is required") String gender,
    @NotBlank(message = "contact is required") String contact) {
}

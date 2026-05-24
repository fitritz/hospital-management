package com.local.hospitalapi.model;

import jakarta.validation.constraints.NotBlank;

public record DoctorRequest(
    @NotBlank(message = "name is required") String name,
    @NotBlank(message = "specialization is required") String specialization,
    @NotBlank(message = "contact is required") String contact) {
}

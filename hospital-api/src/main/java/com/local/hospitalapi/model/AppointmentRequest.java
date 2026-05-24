package com.local.hospitalapi.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AppointmentRequest(
    @Min(value = 1, message = "patientId must be >= 1") int patientId,
    @Min(value = 1, message = "doctorId must be >= 1") int doctorId,
    @NotBlank(message = "scheduledAt is required") String scheduledAt) {
}

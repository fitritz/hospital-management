package com.local.hospitalapi.model;

public record AppointmentView(
    int id,
    int patientId,
    String patientName,
    int doctorId,
    String doctorName,
    String scheduledAt) {
}

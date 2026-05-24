package com.local.hospitalapi.model;

public record Appointment(int id, int patientId, int doctorId, String scheduledAt) {
}

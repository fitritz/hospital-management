package com.local.hospitalapi.controller;

import com.local.hospitalapi.repository.HospitalJdbcRepository;
import com.local.hospitalapi.repository.HospitalMongoRepository;
import com.local.hospitalapi.repository.HospitalRepository;
import com.local.hospitalapi.model.Patient;
import com.local.hospitalapi.model.Doctor;
import com.local.hospitalapi.model.Appointment;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

  private final HospitalJdbcRepository jdbc;
  private final HospitalMongoRepository mongo;

  public AdminController(HospitalJdbcRepository jdbc, HospitalMongoRepository mongo) {
    this.jdbc = jdbc;
    this.mongo = mongo;
  }

  @PostMapping("/migrate")
  public ResponseEntity<String> migrate() {
    if (mongo == null)
      return ResponseEntity.status(500).body("Mongo not configured");

    List<Patient> patients = jdbc.loadPatients();
    for (Patient p : patients) {
      mongo.insertPatient(p.name(), p.age(), p.gender(), p.contact());
    }

    List<Doctor> doctors = jdbc.loadDoctors();
    for (Doctor d : doctors) {
      mongo.insertDoctor(d.name(), d.specialization(), d.contact());
    }

    List<com.local.hospitalapi.model.AppointmentView> apps = jdbc.loadAppointments();
    for (com.local.hospitalapi.model.AppointmentView a : apps) {
      mongo.insertAppointment(a.patientId(), a.doctorId(), a.scheduledAt());
    }

    return ResponseEntity.ok("migration complete");
  }
}

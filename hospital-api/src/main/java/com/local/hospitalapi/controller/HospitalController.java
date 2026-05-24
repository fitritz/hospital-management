package com.local.hospitalapi.controller;

import com.local.hospitalapi.model.Appointment;
import com.local.hospitalapi.model.AppointmentRequest;
import com.local.hospitalapi.model.AppointmentView;
import com.local.hospitalapi.model.Doctor;
import com.local.hospitalapi.model.DoctorRequest;
import com.local.hospitalapi.model.Patient;
import com.local.hospitalapi.model.PatientRequest;
import com.local.hospitalapi.repository.HospitalJdbcRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HospitalController {

  private final HospitalJdbcRepository repository;

  public HospitalController(HospitalJdbcRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping("/patients")
  public List<Patient> getPatients() {
    return repository.loadPatients();
  }

  @PostMapping("/patients")
  public ResponseEntity<Patient> createPatient(@Valid @RequestBody PatientRequest request) {
    Patient created = repository.insertPatient(request.name(), request.age(), request.gender(), request.contact());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @DeleteMapping("/patients/{id}")
  public ResponseEntity<Void> deletePatient(@PathVariable int id) {
    return repository.deletePatient(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  @GetMapping("/doctors")
  public List<Doctor> getDoctors() {
    return repository.loadDoctors();
  }

  @PostMapping("/doctors")
  public ResponseEntity<Doctor> createDoctor(@Valid @RequestBody DoctorRequest request) {
    Doctor created = repository.insertDoctor(request.name(), request.specialization(), request.contact());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @DeleteMapping("/doctors/{id}")
  public ResponseEntity<Void> deleteDoctor(@PathVariable int id) {
    return repository.deleteDoctor(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  @GetMapping("/appointments")
  public List<AppointmentView> getAppointments() {
    return repository.loadAppointments();
  }

  @PostMapping("/appointments")
  public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentRequest request) {
    Appointment created = repository.insertAppointment(request.patientId(), request.doctorId(), request.scheduledAt());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @DeleteMapping("/appointments/{id}")
  public ResponseEntity<Void> deleteAppointment(@PathVariable int id) {
    return repository.deleteAppointment(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}

package com.local.hospitalapi.repository;

import com.local.hospitalapi.model.Appointment;
import com.local.hospitalapi.model.AppointmentView;
import com.local.hospitalapi.model.Doctor;
import com.local.hospitalapi.model.Patient;
import java.util.List;

public interface HospitalRepository {
  List<Patient> loadPatients();
  List<Doctor> loadDoctors();
  List<AppointmentView> loadAppointments();

  Patient insertPatient(String name, int age, String gender, String contact);
  Doctor insertDoctor(String name, String specialization, String contact);
  Appointment insertAppointment(int patientId, int doctorId, String scheduledAt);

  boolean deletePatient(int id);
  boolean deleteDoctor(int id);
  boolean deleteAppointment(int id);
}

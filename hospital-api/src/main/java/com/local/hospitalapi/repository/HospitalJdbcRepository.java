package com.local.hospitalapi.repository;

import com.local.hospitalapi.model.Appointment;
import com.local.hospitalapi.model.AppointmentView;
import com.local.hospitalapi.model.Doctor;
import com.local.hospitalapi.model.Patient;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalJdbcRepository implements HospitalRepository {

  private final JdbcTemplate jdbcTemplate;

  public HospitalJdbcRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Patient> loadPatients() {
    return jdbcTemplate.query(
        "SELECT id, name, age, gender, contact FROM patients ORDER BY id",
        (rs, rowNum) -> new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("contact")));
  }

  public List<Doctor> loadDoctors() {
    return jdbcTemplate.query(
        "SELECT id, name, specialization, contact FROM doctors ORDER BY id",
        (rs, rowNum) -> new Doctor(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("specialization"),
            rs.getString("contact")));
  }

  public List<AppointmentView> loadAppointments() {
    return jdbcTemplate.query(
        """
        SELECT a.id, a.patient_id, a.doctor_id, a.scheduled_at,
               p.name AS patient_name,
               d.name AS doctor_name
        FROM appointments a
        JOIN patients p ON p.id = a.patient_id
        JOIN doctors d ON d.id = a.doctor_id
        ORDER BY a.id
        """,
        (rs, rowNum) -> new AppointmentView(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getString("patient_name"),
            rs.getInt("doctor_id"),
            rs.getString("doctor_name"),
            rs.getString("scheduled_at")));
  }

  public Patient insertPatient(String name, int age, String gender, String contact) {
    long id = insertAndReturnId(
        "INSERT INTO patients (name, age, gender, contact) VALUES (?, ?, ?, ?)",
        name, age, gender, contact);
    return new Patient((int) id, name, age, gender, contact);
  }

  public Doctor insertDoctor(String name, String specialization, String contact) {
    long id = insertAndReturnId(
        "INSERT INTO doctors (name, specialization, contact) VALUES (?, ?, ?)",
        name, specialization, contact);
    return new Doctor((int) id, name, specialization, contact);
  }

  public Appointment insertAppointment(int patientId, int doctorId, String scheduledAt) {
    long id = insertAndReturnId(
        "INSERT INTO appointments (patient_id, doctor_id, scheduled_at) VALUES (?, ?, ?)",
        patientId, doctorId, scheduledAt);
    return new Appointment((int) id, patientId, doctorId, scheduledAt);
  }

  public boolean deletePatient(int id) {
    return jdbcTemplate.update("DELETE FROM patients WHERE id = ?", id) > 0;
  }

  public boolean deleteDoctor(int id) {
    return jdbcTemplate.update("DELETE FROM doctors WHERE id = ?", id) > 0;
  }

  public boolean deleteAppointment(int id) {
    return jdbcTemplate.update("DELETE FROM appointments WHERE id = ?", id) > 0;
  }

  private long insertAndReturnId(String sql, Object... params) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      for (int i = 0; i < params.length; i++) {
        statement.setObject(i + 1, params[i]);
      }
      return statement;
    }, keyHolder);

    if (keyHolder.getKey() == null) {
      throw new IllegalStateException("Insert succeeded but generated key was not returned.");
    }

    return keyHolder.getKey().longValue();
  }
}

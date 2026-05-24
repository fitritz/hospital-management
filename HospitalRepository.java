import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HospitalRepository {

  public HospitalRepository() {
    try {
      HospitalDatabase.initialize();
    } catch (SQLException exception) {
      throw new IllegalStateException("Failed to initialize hospital database.", exception);
    }
  }

  public List<HospitalManagementFX.Patient> loadPatients() throws SQLException {
    List<HospitalManagementFX.Patient> patients = new ArrayList<>();
    String sql = "SELECT id, name, age, gender, contact FROM patients ORDER BY id";

    try (Connection connection = HospitalDatabase.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {

      while (resultSet.next()) {
        patients.add(new HospitalManagementFX.Patient(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getInt("age"),
            resultSet.getString("gender"),
            resultSet.getString("contact")));
      }
    }

    return patients;
  }

  public List<HospitalManagementFX.Doctor> loadDoctors() throws SQLException {
    List<HospitalManagementFX.Doctor> doctors = new ArrayList<>();
    String sql = "SELECT id, name, specialization, contact FROM doctors ORDER BY id";

    try (Connection connection = HospitalDatabase.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {

      while (resultSet.next()) {
        doctors.add(new HospitalManagementFX.Doctor(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("specialization"),
            resultSet.getString("contact")));
      }
    }

    return doctors;
  }

  public List<HospitalManagementFX.Appointment> loadAppointments() throws SQLException {
    List<HospitalManagementFX.Appointment> appointments = new ArrayList<>();
    String sql = "SELECT id, patient_id, doctor_id, scheduled_at FROM appointments ORDER BY id";

    try (Connection connection = HospitalDatabase.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {

      while (resultSet.next()) {
        appointments.add(new HospitalManagementFX.Appointment(
            resultSet.getInt("id"),
            resultSet.getInt("patient_id"),
            resultSet.getInt("doctor_id"),
            resultSet.getString("scheduled_at")));
      }
    }

    return appointments;
  }

  public HospitalManagementFX.Patient insertPatient(String name, int age, String gender, String contact)
      throws SQLException {
    String sql = "INSERT INTO patients (name, age, gender, contact) VALUES (?, ?, ?, ?)";
    try (Connection connection = HospitalDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setInt(2, age);
      statement.setString(3, gender);
      statement.setString(4, contact);
      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          return new HospitalManagementFX.Patient(keys.getInt(1), name, age, gender, contact);
        }
      }
    }

    throw new SQLException("Failed to insert patient.");
  }

  public HospitalManagementFX.Doctor insertDoctor(String name, String specialization, String contact)
      throws SQLException {
    String sql = "INSERT INTO doctors (name, specialization, contact) VALUES (?, ?, ?)";
    try (Connection connection = HospitalDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, specialization);
      statement.setString(3, contact);
      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          return new HospitalManagementFX.Doctor(keys.getInt(1), name, specialization, contact);
        }
      }
    }

    throw new SQLException("Failed to insert doctor.");
  }

  public HospitalManagementFX.Appointment insertAppointment(int patientId, int doctorId, String scheduledAt)
      throws SQLException {
    String sql = "INSERT INTO appointments (patient_id, doctor_id, scheduled_at) VALUES (?, ?, ?)";
    try (Connection connection = HospitalDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, patientId);
      statement.setInt(2, doctorId);
      statement.setString(3, scheduledAt);
      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          return new HospitalManagementFX.Appointment(keys.getInt(1), patientId, doctorId, scheduledAt);
        }
      }
    }

    throw new SQLException("Failed to insert appointment.");
  }

  public boolean deletePatient(int id) throws SQLException {
    return deleteById("DELETE FROM patients WHERE id = ?", id);
  }

  public boolean deleteDoctor(int id) throws SQLException {
    return deleteById("DELETE FROM doctors WHERE id = ?", id);
  }

  public boolean deleteAppointment(int id) throws SQLException {
    return deleteById("DELETE FROM appointments WHERE id = ?", id);
  }

  public int getNextPatientId() throws SQLException {
    return nextId("patients");
  }

  public int getNextDoctorId() throws SQLException {
    return nextId("doctors");
  }

  public int getNextAppointmentId() throws SQLException {
    return nextId("appointments");
  }

  private boolean deleteById(String sql, int id) throws SQLException {
    try (Connection connection = HospitalDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, id);
      return statement.executeUpdate() > 0;
    }
  }

  private int nextId(String tableName) throws SQLException {
    String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM " + tableName;
    try (Connection connection = HospitalDatabase.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {
      return resultSet.next() ? resultSet.getInt(1) : 1;
    }
  }
}

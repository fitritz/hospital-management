import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class HospitalDatabase {

  private static final Path DB_PATH = Paths.get(System.getProperty("user.dir"), "data", "hospital.db");
  private static final String JDBC_URL = "jdbc:sqlite:"
      + DB_PATH.toAbsolutePath().normalize().toString().replace("\\", "/");

  private static final String CREATE_PATIENTS_TABLE = """
      CREATE TABLE IF NOT EXISTS patients (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        age INTEGER NOT NULL,
        gender TEXT NOT NULL,
        contact TEXT NOT NULL
      )
      """;

  private static final String CREATE_DOCTORS_TABLE = """
      CREATE TABLE IF NOT EXISTS doctors (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        specialization TEXT NOT NULL,
        contact TEXT NOT NULL
      )
      """;

  private static final String CREATE_APPOINTMENTS_TABLE = """
      CREATE TABLE IF NOT EXISTS appointments (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        patient_id INTEGER NOT NULL,
        doctor_id INTEGER NOT NULL,
        scheduled_at TEXT NOT NULL,
        FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
        FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
      )
      """;

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException exception) {
      throw new IllegalStateException("SQLite JDBC driver not found. Add sqlite-jdbc to the classpath.", exception);
    }
  }

  private HospitalDatabase() {
  }

  public static void initialize() throws SQLException {
    ensureDataDirectory();
    try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA foreign_keys = ON");
      statement.executeUpdate(CREATE_PATIENTS_TABLE);
      statement.executeUpdate(CREATE_DOCTORS_TABLE);
      statement.executeUpdate(CREATE_APPOINTMENTS_TABLE);
    }
  }

  public static Connection getConnection() throws SQLException {
    ensureDataDirectory();
    Connection connection = DriverManager.getConnection(JDBC_URL);
    try (Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA foreign_keys = ON");
    }
    return connection;
  }

  public static Path getDatabaseFile() {
    return DB_PATH;
  }

  private static void ensureDataDirectory() throws SQLException {
    try {
      Files.createDirectories(DB_PATH.getParent());
    } catch (IOException exception) {
      throw new SQLException("Failed to create database directory.", exception);
    }
  }
}

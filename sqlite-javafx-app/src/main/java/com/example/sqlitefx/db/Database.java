package com.example.sqlitefx.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

  private static final Path DB_PATH = Paths.get(System.getProperty("user.dir"), "data", "users.db");
  private static final String JDBC_PREFIX = "jdbc:sqlite:";
  private static final String CREATE_USERS_TABLE_SQL = """
      CREATE TABLE IF NOT EXISTS users (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          full_name TEXT NOT NULL,
          email TEXT NOT NULL UNIQUE,
          phone TEXT,
          created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
      )
      """;

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException exception) {
      throw new IllegalStateException("SQLite JDBC driver is missing from the classpath.", exception);
    }
  }

  private Database() {
  }

  public static String getJdbcUrl() {
    return JDBC_PREFIX + DB_PATH.toAbsolutePath().normalize().toString().replace("\\", "/");
  }

  public static Connection getConnection() throws SQLException {
    ensureDataDirectory();
    return DriverManager.getConnection(getJdbcUrl());
  }

  public static void initialize() throws SQLException {
    ensureDataDirectory();
    try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
      statement.executeUpdate(CREATE_USERS_TABLE_SQL);
    }
  }

  public static Path getDatabaseFile() {
    return DB_PATH;
  }

  private static void ensureDataDirectory() throws SQLException {
    try {
      Path parent = DB_PATH.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
    } catch (IOException exception) {
      throw new SQLException("Failed to create SQLite data directory.", exception);
    }
  }
}

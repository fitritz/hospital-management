package com.local.hospitalapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostConstruct
  public void initialize() {
    jdbcTemplate.execute("PRAGMA foreign_keys = ON");

    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS patients (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          name TEXT NOT NULL,
          age INTEGER NOT NULL,
          gender TEXT NOT NULL,
          contact TEXT NOT NULL
        )
        """);

    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS doctors (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          name TEXT NOT NULL,
          specialization TEXT NOT NULL,
          contact TEXT NOT NULL
        )
        """);

    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS appointments (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          patient_id INTEGER NOT NULL,
          doctor_id INTEGER NOT NULL,
          scheduled_at TEXT NOT NULL,
          FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
          FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
        )
        """);
  }
}

package com.local.hospitalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class HospitalApiApplication {

  private static final String LOCAL_DB_PATH = "../data/hospital.db";
  private static final String RAILWAY_DB_PATH = "/tmp/hospital.db";

  public static void main(String[] args) {
    String configuredDbPath = System.getenv("SQLITE_DB_PATH");
    if (configuredDbPath == null || configuredDbPath.isBlank()) {
      configuredDbPath = System.getenv("PORT") != null ? RAILWAY_DB_PATH : LOCAL_DB_PATH;
    }

    Path dbPath = Paths.get(configuredDbPath).toAbsolutePath().normalize();
    Path parentDirectory = dbPath.getParent();
    if (parentDirectory != null) {
      try {
        Files.createDirectories(parentDirectory);
      } catch (Exception exception) {
        throw new IllegalStateException("Unable to create database directory: " + parentDirectory, exception);
      }
    }

    System.setProperty("spring.datasource.url", "jdbc:sqlite:" + dbPath);
    SpringApplication.run(HospitalApiApplication.class, args);
  }
}

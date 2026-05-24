package com.local.hospitalapi.repository;

import com.local.hospitalapi.model.Appointment;
import com.local.hospitalapi.model.AppointmentView;
import com.local.hospitalapi.model.Doctor;
import com.local.hospitalapi.model.Patient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("mongoRepository")
public class HospitalMongoRepository implements HospitalRepository {

  private final MongoClient client;
  private final MongoDatabase database;
  private final MongoCollection<Document> patients;
  private final MongoCollection<Document> doctors;
  private final MongoCollection<Document> appointments;

  public HospitalMongoRepository() {
    String uri = System.getProperty("hospital.mongo.uri");
    if (uri == null || uri.isBlank()) {
      uri = System.getenv("MONGO_URI");
    }
    if (uri == null || uri.isBlank()) {
      client = null;
      database = null;
      patients = null;
      doctors = null;
      appointments = null;
      return;
    }

    this.client = MongoClients.create(uri);
    String dbName = System.getProperty("hospital.mongo.database");
    if (dbName == null || dbName.isBlank()) {
      dbName = System.getenv("MONGO_DATABASE");
    }
    if (dbName == null || dbName.isBlank()) {
      dbName = "hospital_management";
    }

    this.database = client.getDatabase(dbName);
    this.patients = database.getCollection("patients");
    this.doctors = database.getCollection("doctors");
    this.appointments = database.getCollection("appointments");
  }

  private int nextId(MongoCollection<Document> coll) {
    Document last = coll.find().sort(Sorts.descending("id")).projection(new Document("id", 1)).first();
    if (last == null)
      return 1;
    Integer id = last.getInteger("id");
    return id == null ? 1 : id + 1;
  }

  @Override
  public List<Patient> loadPatients() {
    if (patients == null)
      return new ArrayList<>();
    List<Patient> result = new ArrayList<>();
    for (Document d : patients.find().sort(Sorts.ascending("id"))) {
      result.add(new Patient(
          d.getInteger("id", 0),
          d.getString("name"),
          d.getInteger("age", 0),
          d.getString("gender"),
          d.getString("contact")));
    }
    return result;
  }

  @Override
  public List<Doctor> loadDoctors() {
    if (doctors == null)
      return new ArrayList<>();
    List<Doctor> result = new ArrayList<>();
    for (Document d : doctors.find().sort(Sorts.ascending("id"))) {
      result.add(new Doctor(
          d.getInteger("id", 0),
          d.getString("name"),
          d.getString("specialization"),
          d.getString("contact")));
    }
    return result;
  }

  @Override
  public List<AppointmentView> loadAppointments() {
    if (appointments == null)
      return new ArrayList<>();
    List<AppointmentView> result = new ArrayList<>();
    for (Document d : appointments.find().sort(Sorts.ascending("id"))) {
      int id = d.getInteger("id", 0);
      int patientId = d.getInteger("patientId", 0);
      int doctorId = d.getInteger("doctorId", 0);
      String scheduledAt = d.getString("scheduledAt");
      String patientName = "";
      String doctorName = "";
      Document p = patients.find(Filters.eq("id", patientId)).first();
      if (p != null)
        patientName = p.getString("name");
      Document dr = doctors.find(Filters.eq("id", doctorId)).first();
      if (dr != null)
        doctorName = dr.getString("name");
      result.add(new AppointmentView(id, patientId, patientName, doctorId, doctorName, scheduledAt));
    }
    return result;
  }

  @Override
  public Patient insertPatient(String name, int age, String gender, String contact) {
    if (patients == null)
      throw new IllegalStateException("Mongo not configured");
    int id = nextId(patients);
    Document d = new Document("id", id)
        .append("name", name)
        .append("age", age)
        .append("gender", gender)
        .append("contact", contact);
    patients.insertOne(d);
    return new Patient(id, name, age, gender, contact);
  }

  @Override
  public Doctor insertDoctor(String name, String specialization, String contact) {
    if (doctors == null)
      throw new IllegalStateException("Mongo not configured");
    int id = nextId(doctors);
    Document d = new Document("id", id)
        .append("name", name)
        .append("specialization", specialization)
        .append("contact", contact);
    doctors.insertOne(d);
    return new Doctor(id, name, specialization, contact);
  }

  @Override
  public Appointment insertAppointment(int patientId, int doctorId, String scheduledAt) {
    if (appointments == null)
      throw new IllegalStateException("Mongo not configured");
    int id = nextId(appointments);
    Document d = new Document("id", id)
        .append("patientId", patientId)
        .append("doctorId", doctorId)
        .append("scheduledAt", scheduledAt);
    appointments.insertOne(d);
    return new Appointment(id, patientId, doctorId, scheduledAt);
  }

  @Override
  public boolean deletePatient(int id) {
    if (patients == null)
      return false;
    var res = patients.deleteOne(Filters.eq("id", id));
    appointments.deleteMany(Filters.eq("patientId", id));
    return res.getDeletedCount() > 0;
  }

  @Override
  public boolean deleteDoctor(int id) {
    if (doctors == null)
      return false;
    var res = doctors.deleteOne(Filters.eq("id", id));
    appointments.deleteMany(Filters.eq("doctorId", id));
    return res.getDeletedCount() > 0;
  }

  @Override
  public boolean deleteAppointment(int id) {
    if (appointments == null)
      return false;
    var res = appointments.deleteOne(Filters.eq("id", id));
    return res.getDeletedCount() > 0;
  }
}

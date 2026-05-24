import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ApiClient {
  private final MongoClient mongoClient;
  private final MongoDatabase database;
  private final MongoCollection<Document> patientCollection;
  private final MongoCollection<Document> doctorCollection;
  private final MongoCollection<Document> appointmentCollection;
  private final String connectionDescription;

  public ApiClient(String mongoUri, String databaseName, String patientsCollectionName, String doctorsCollectionName,
      String appointmentsCollectionName) {
    this.mongoClient = MongoClients.create(mongoUri);
    this.database = mongoClient.getDatabase(databaseName);
    this.patientCollection = database.getCollection(patientsCollectionName);
    this.doctorCollection = database.getCollection(doctorsCollectionName);
    this.appointmentCollection = database.getCollection(appointmentsCollectionName);
    this.connectionDescription = mongoUri + " / " + databaseName;
  }

  public String getBaseUrl() {
    return connectionDescription;
  }

  public List<HospitalManagementFX.Patient> loadPatients() {
    List<HospitalManagementFX.Patient> result = new ArrayList<>();
    for (Document document : patientCollection.find().sort(Sorts.ascending("id"))) {
      result.add(toPatient(document));
    }
    return result;
  }

  public List<HospitalManagementFX.Doctor> loadDoctors() {
    List<HospitalManagementFX.Doctor> result = new ArrayList<>();
    for (Document document : doctorCollection.find().sort(Sorts.ascending("id"))) {
      result.add(toDoctor(document));
    }
    return result;
  }

  public List<HospitalManagementFX.Appointment> loadAppointments() {
    List<HospitalManagementFX.Appointment> result = new ArrayList<>();
    for (Document document : appointmentCollection.find().sort(Sorts.ascending("id"))) {
      result.add(toAppointment(document));
    }
    return result;
  }

  public HospitalManagementFX.Patient insertPatient(String name, int age, String gender, String contact) {
    int id = nextId(patientCollection);
    Document document = new Document("id", id)
        .append("name", name)
        .append("age", age)
        .append("gender", gender)
        .append("contact", contact);
    patientCollection.insertOne(document);
    return new HospitalManagementFX.Patient(id, name, age, gender, contact);
  }

  public HospitalManagementFX.Doctor insertDoctor(String name, String specialization, String contact) {
    int id = nextId(doctorCollection);
    Document document = new Document("id", id)
        .append("name", name)
        .append("specialization", specialization)
        .append("contact", contact);
    doctorCollection.insertOne(document);
    return new HospitalManagementFX.Doctor(id, name, specialization, contact);
  }

  public HospitalManagementFX.Appointment insertAppointment(int patientId, int doctorId, String scheduledAt) {
    int id = nextId(appointmentCollection);
    Document document = new Document("id", id)
        .append("patientId", patientId)
        .append("doctorId", doctorId)
        .append("scheduledAt", scheduledAt);
    appointmentCollection.insertOne(document);
    return new HospitalManagementFX.Appointment(id, patientId, doctorId, scheduledAt);
  }

  public boolean deletePatient(int id) {
    boolean deleted = patientCollection.deleteOne(Filters.eq("id", id)).getDeletedCount() > 0;
    if (deleted) {
      appointmentCollection.deleteMany(Filters.eq("patientId", id));
    }
    return deleted;
  }

  public boolean deleteDoctor(int id) {
    boolean deleted = doctorCollection.deleteOne(Filters.eq("id", id)).getDeletedCount() > 0;
    if (deleted) {
      appointmentCollection.deleteMany(Filters.eq("doctorId", id));
    }
    return deleted;
  }

  public boolean deleteAppointment(int id) {
    return appointmentCollection.deleteOne(Filters.eq("id", id)).getDeletedCount() > 0;
  }

  public boolean updatePatient(int id, String name, int age, String gender, String contact) {
    Document update = new Document("$set", new Document()
        .append("name", name)
        .append("age", age)
        .append("gender", gender)
        .append("contact", contact));
    return patientCollection.updateOne(Filters.eq("id", id), update).getModifiedCount() > 0;
  }

  private int nextId(MongoCollection<Document> collection) {
    Document last = collection.find().sort(Sorts.descending("id")).projection(new Document("id", 1)).first();
    if (last == null) {
      return 1;
    }

    Integer current = last.getInteger("id");
    return current == null ? 1 : current + 1;
  }

  private HospitalManagementFX.Patient toPatient(Document document) {
    return new HospitalManagementFX.Patient(
        document.getInteger("id", 0),
        document.getString("name"),
        document.getInteger("age", 0),
        document.getString("gender"),
        document.getString("contact"));
  }

  private HospitalManagementFX.Doctor toDoctor(Document document) {
    return new HospitalManagementFX.Doctor(
        document.getInteger("id", 0),
        document.getString("name"),
        document.getString("specialization"),
        document.getString("contact"));
  }

  private HospitalManagementFX.Appointment toAppointment(Document document) {
    return new HospitalManagementFX.Appointment(
        document.getInteger("id", 0),
        document.getInteger("patientId", 0),
        document.getInteger("doctorId", 0),
        document.getString("scheduledAt"));
  }
}

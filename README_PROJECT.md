**Hospital Management System — Project Overview**

This repository is a lightweight Java-based Hospital Management System with a JavaFX frontend. The current app uses MongoDB as its primary datastore, and the older SQLite repository is still present in the codebase as a fallback/reference implementation.

**Key Components**
- **Frontend:** `HospitalManagementFX.java` — JavaFX application providing UI for patients, doctors, and appointments.
- **MongoDB client (current):** implemented inside `HospitalManagementFX.java` as `ApiClient` — loads and writes patient, doctor, and appointment documents directly to MongoDB.
- **Local store (legacy/reference):** `HospitalDatabase.java` and `HospitalRepository.java` — an embedded SQLite database stored at `data/hospital.db` and a simple JDBC repository.
- **Utilities & scripts:** `run_hospital.bat` (Windows wrapper to launch via Maven), `run_db_test.bat` and `DbConnect.java` (MySQL connection test helper).

**Architecture (high level)**

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the architecture diagram and detailed component interactions.

**How the app works (summary)**
- On startup the JavaFX application initializes UI components and attempts to load data.
- By default it uses MongoDB: `HospitalManagementFX` connects to a MongoDB database and performs CRUD on the `patients`, `doctors`, and `appointments` collections.
- Mongo settings are controlled by system properties:
	- `-Dhospital.mongo.uri=mongodb://localhost:27017`
	- `-Dhospital.mongo.database=hospital_management`
	- Optional collection overrides: `hospital.mongo.patientsCollection`, `hospital.mongo.doctorsCollection`, `hospital.mongo.appointmentsCollection`

**Run (Windows)**
- Quick (recommended):
```
run_hospital.bat
```
- Direct Maven (with optional API URL):
```
mvn -f pom.xml -Dhospital.mongo.uri="mongodb://localhost:27017" -Dhospital.mongo.database="hospital_management" clean javafx:run
```

**Run DB connectivity test (MySQL)**
```
run_db_test.bat jdbc:mysql://HOST:3306/DATABASE dbuser dbpass
```

**Storage choice guidance**
- SQLite (embedded): still available in the repo, but best only for local/single-machine usage.
- MongoDB: the active deployment mode in the app. Good when you need document storage and a separate MongoDB service.

**Where to look in code**
- UI: [HospitalManagementFX.java](HospitalManagementFX.java)
- Local DB bootstrap: [HospitalDatabase.java](HospitalDatabase.java)
- JDBC repository: [HospitalRepository.java](HospitalRepository.java)
- MongoDB client: [HospitalManagementFX.java](HospitalManagementFX.java)
- Maven build / JavaFX config: [pom.xml](pom.xml)
- Scripts: [run_hospital.bat](run_hospital.bat), [run_db_test.bat](run_db_test.bat)

If you'd like, I can now:
- Add PRAGMA tuning for SQLite in `HospitalDatabase.java` (WAL + synchronous), or
- Scaffold a `HospitalStore` interface plus a `MongoHospitalRepository` and migration tool.
Tell me which next step you'd prefer.

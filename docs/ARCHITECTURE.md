# Architecture — Hospital Management System

The diagram below shows the high-level architecture of this project and how its components interact.

```mermaid
flowchart LR
  UI[JavaFX Client\n(HospitalManagementFX)] -->|Reads/Writes| Mongo[(MongoDB)\n(patients / doctors / appointments)]
  UI -->|Legacy fallback| LocalDB[SQLite\n(data/hospital.db)\n(HospitalRepository)]
  LocalDB -->|Backup / Migration| Mongo
  subgraph Scripts
    RH[run_hospital.bat]
    RDB[run_db_test.bat]
  end
  UI --- RH
  DbTest[DbConnect.java] --- RDB
```

## Components

- **JavaFX Client (`HospitalManagementFX.java`)**
  - Primary user interface. Manages UI state (patients, doctors, appointments) and displays an activity log.
  - Current operating mode:
    - **Mongo-backed:** uses `ApiClient` to connect directly to MongoDB. Connection settings are configurable with `-Dhospital.mongo.uri` and `-Dhospital.mongo.database`.
  - Legacy fallback:
    - **SQLite repository:** `HospitalRepository` still exists for local embedded use and migration support.

- **Local storage (`HospitalDatabase.java`, `HospitalRepository.java`)**
  - `HospitalDatabase` handles database file location and schema creation.
  - `HospitalRepository` performs CRUD operations over JDBC using `HospitalDatabase.getConnection()`.
  - The DB file lives under `data/hospital.db` by default.

-- **MongoDB**
  - The active datastore.
  - Stores `patients`, `doctors`, and `appointments` as documents in a MongoDB database.

- **Utilities & Scripts**
  - `run_hospital.bat` — convenience wrapper that sets `JAVA_HOME` and runs `mvn javafx:run`.
  - `run_db_test.bat` and `DbConnect.java` — helper to test MySQL connectivity.

## Data flow

1. On startup the client loads data either from the local DB or the remote API.
2. UI actions (create/delete appointments, patients, doctors) are executed against the selected store.
3. If the app is running in local mode, the SQLite file is the single source of truth.
4. To move to a server-backed deployment, a migration copies records from the SQLite file to the server DB.

## Deployment Topologies

- Single-machine (small install): JavaFX client + local MongoDB instance.
- Client-server: JavaFX client(s) + centralized MongoDB service — recommended for multiple users, high concurrency, and HA.

## Notes about concurrency and scaling

- SQLite is still ACID but not designed for high concurrent write workloads: keep it only for migration or fallback use.
- MongoDB can scale well for many document-style workloads, but you still need proper authentication, backups, and monitoring.

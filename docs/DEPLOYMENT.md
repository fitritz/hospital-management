# Deployment & Migration Guide

This guide explains deployment considerations for the current MongoDB-backed app, plus the remaining SQLite fallback and migration options.

## Current runtime settings

- MongoDB URI: `-Dhospital.mongo.uri=mongodb://localhost:27017`
- MongoDB database: `-Dhospital.mongo.database=hospital_management`
- Optional collection overrides:
  - `-Dhospital.mongo.patientsCollection=patients`
  - `-Dhospital.mongo.doctorsCollection=doctors`
  - `-Dhospital.mongo.appointmentsCollection=appointments`

## Local `.env` file

The Java app now reads a local `.env` file from the project root if present. Use it for local development only.

Example keys:

```env
MONGO_URI=mongodb+srv://<user>:<password>@<cluster>/<dbname>?retryWrites=true&w=majority
MONGO_DATABASE=hospital_management
MONGO_PATIENTS_COLLECTION=patients
MONGO_DOCTORS_COLLECTION=doctors
MONGO_APPOINTMENTS_COLLECTION=appointments
```

Render does not use your local `.env` file automatically. For Render, add the same values in the service environment variables dashboard.

## SQLite in production — practical tips

- **WAL mode for better concurrency:** Enable Write-Ahead Logging to improve reader/writer concurrency:
  - Example SQL to run once on initialization: `PRAGMA journal_mode = WAL;`.
- **Choose synchronous level carefully:** `PRAGMA synchronous = NORMAL` gives good performance with reasonable durability; `FULL` is safest but slower.
- **File permissions & encryption:** Protect `data/hospital.db` with OS file permissions. For encryption use SQLCipher or run on disk-level encryption (BitLocker/LUKS).
- **Backups:** Use the SQLite online backup API or `sqlite3` backup commands. Avoid copying the DB file while a write is in progress — use the backup API for safe copies.
- **Avoid shared network filesystems:** Do not host the SQLite file on SMB/NFS between multiple machines — this leads to corruption. Use a server DB for multi-machine access.

## Running on a single desktop (quick)

1. Ensure JDK 17+ is installed and `run_hospital.bat` points to it or your PATH contains `java` and `mvn`.
2. From project root run:
```
run_hospital.bat
```
3. Start MongoDB locally before launching the app, or override `hospital.mongo.uri` to point at a reachable MongoDB server.

## Running a multi-user deployment (recommended for clinics/hospitals)

1. Deploy MongoDB as a managed service or secured cluster.
2. Configure the UI with Render environment variables or system properties:
  - `MONGO_URI` or `-Dhospital.mongo.uri="mongodb://..."`
  - `MONGO_DATABASE` or `-Dhospital.mongo.database="..."`
3. Protect MongoDB with authentication, TLS, network rules, logging, and backups.

## Migration plan — SQLite → Server DB (Postgres/MySQL)

1. **Add a repository abstraction**: introduce `HospitalStore` interface in code, keep current `HospitalRepository` as SQLite implementation.
2. **Implement server-backed repository**: `PostgresHospitalRepository` using JDBC or an ORM.
3. **Migration tool**: create a small CLI that reads from SQLite via JDBC and writes to the server DB. Keep it idempotent (skip existing records).
4. **Staging verification**: run the migration on a copy of the DB, run integration tests and manual verification.
5. **Switch clients**: update clients to start using the API-backed mode and decommission direct SQLite use.

## Migration plan — SQLite → MongoDB (document store)

1. Decide object mapping: keep relational IDs (patientId/doctorId) or embed related documents depending on query patterns.
2. Add the MongoDB Java driver dependency in `pom.xml`:
```
<dependency>
  <groupId>org.mongodb</groupId>
  <artifactId>mongodb-driver-sync</artifactId>
  <version>4.11.1</version>
</dependency>
```
3. Implement `MongoHospitalRepository` that maps `patients`, `doctors`, `appointments` to collections.
4. Create a migration CLI: read rows from SQLite and upsert them into Mongo collections, converting types as needed.

## Testing & verification

- Create a staging instance of the backend DB and run the migration. Verify counts and sample records.
- Run functional tests for UI flows (add patient, add appointment, delete) against the migrated data.

## Monitoring & backups

- For server DBs use scheduled backups (pg_dump for Postgres, native snapshot/backups for managed services). For MongoDB, use `mongodump` or cloud snapshots.
- Add monitoring (Prometheus, Datadog) and alerts for disk usage, replication lag, and connection errors.

---

If you'd like, I can implement the `HospitalStore` abstraction and scaffold a `MongoHospitalRepository` or `PostgresHospitalRepository` and a migration tool next. Which target DB should I scaffold for? (Postgres or MongoDB?)

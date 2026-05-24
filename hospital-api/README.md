# Hospital API (Spring Boot)

This module provides a REST API between your JavaFX frontend and the SQLite database.

## Run

```bat
cd hospital-api
mvnw.cmd spring-boot:run
```

Or run:

```bat
hospital-api\run_api.bat
```

## Database

Uses existing SQLite file at:

`../data/hospital.db`

(From `hospital-api`, this resolves to `CRT/data/hospital.db`.)

## Endpoints

- `GET /api/health`
- `GET /api/patients`
- `POST /api/patients`
- `DELETE /api/patients/{id}`
- `GET /api/doctors`
- `POST /api/doctors`
- `DELETE /api/doctors/{id}`
- `GET /api/appointments`
- `POST /api/appointments`
- `DELETE /api/appointments/{id}`

## Postman

Import these files into Postman:

- `postman/Hospital-API.postman_collection.json`
- `postman/Hospital-Local.postman_environment.json`

# JavaFX + SQLite CRUD Demo

This is a beginner-friendly JavaFX application that uses SQLite through JDBC.

## What it does

- Creates the SQLite database file automatically on first run.
- Creates the `users` table automatically if it does not exist.
- Supports Insert, Update, Delete, and Fetch operations.
- Uses a clean Maven project structure.

## Folder structure

```text
sqlite-javafx-app/
├─ pom.xml
├─ run.bat
└─ src/
   └─ main/
      ├─ java/
      │  └─ com/example/sqlitefx/
      │     ├─ App.java
      │     ├─ dao/UserDao.java
      │     ├─ db/Database.java
      │     ├─ model/User.java
      │     └─ ui/UserCrudView.java
      └─ resources/
         └─ com/example/sqlitefx/styles.css
```

## Where the database is created

The database file is created automatically in:

```text
sqlite-javafx-app/data/users.db
```

## How to run

Open Command Prompt or the VS Code terminal in the `sqlite-javafx-app` folder and run:

```bat
mvn clean javafx:run
```

Or double-click `run.bat` on Windows.

## If Maven is not installed

Install Maven or use the VS Code Java extensions that include Maven support.

## SQLite JDBC dependency

This project uses Maven, so the SQLite JDBC driver is added automatically:

```xml
<dependency>
  <groupId>org.xerial</groupId>
  <artifactId>sqlite-jdbc</artifactId>
  <version>3.45.3.0</version>
</dependency>
```

If you prefer a manual JAR setup, download the SQLite JDBC JAR, place it in a `lib/` folder, and add it to your classpath.

## Beginner-friendly notes

- Keep Java files in `src/main/java`.
- Keep CSS and other assets in `src/main/resources`.
- Use the DAO class for all database work.
- Keep UI code separate from database code.
- Use prepared statements for all SQL queries.

This document explains how to test a MySQL connection using the included `mysql-connector-j-9.7.0.jar`.

Files added
- DbConnect.java — small Java program that connects and prints server version.
- run_db_test.bat — Windows script to compile and run the test using the JAR.

Quick CLI steps (Windows)
1. Open a Command Prompt in this folder.
2. Run the batch with arguments:

```bat
run_db_test.bat jdbc:mysql://HOST:3306/DATABASE dbuser dbpass
```

Or set environment variables and run without args:

```bat
set DB_URL=jdbc:mysql://HOST:3306/DATABASE
set DB_USER=dbuser
set DB_PASS=dbpass
run_db_test.bat
```

What the program does
- Connects using the JDBC URL, user and password.
- Prints a confirmation and the MySQL server version.

Common JDBC URL examples
- Local default port, no SSL:
  jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC

Maven/Gradle dependency
- Maven:

```xml
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <version>9.7.0</version>
</dependency>
```

- Gradle (Groovy):

```gradle
implementation 'com.mysql:mysql-connector-j:9.7.0'
```

IDE setup
- IntelliJ: File → Project Structure → Libraries → + → select `mysql-connector-j-9.7.0.jar`.
- VS Code (Java extension): Put the JAR into a `lib/` folder and add it to referenced libraries or update the launch configuration classpath.
- Eclipse: Right-click project → Build Path → Add External Archives → select the JAR.

Database user & permissions (example SQL)
1. Login to MySQL as admin.
2. Create DB and user:

```sql
CREATE DATABASE mydb;
CREATE USER 'appuser'@'%' IDENTIFIED BY 'secret';
GRANT ALL PRIVILEGES ON mydb.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
```

Troubleshooting
- "Access denied for user": verify username/password and host allowed in user grants.
- "Communications link failure": check server is running, port open, and firewall rules.
- "The server time zone value ...": add `serverTimezone=UTC` to the JDBC URL.
- SSL errors: add `useSSL=false` for local testing or configure SSL properly.

Security note
- Do not commit real passwords to source control. Use environment variables or a secure credentials store.

If you want, I can:
- Run the compile step now to verify no syntax errors.
- Attempt a connection if you provide DB host, user, and password (or allow me to use temporary, local test DB).

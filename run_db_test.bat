@echo off
if "%~1"=="" (
  if defined DB_URL (
    set "URL=%DB_URL%"
  ) else (
    echo Provide JDBC URL as first argument or set DB_URL environment variable
    goto :eof
  )
) else set "URL=%~1"
if "%~2"=="" (
  if defined DB_USER (
    set "USER=%DB_USER%"
  ) else (
    echo Provide DB user as second argument or set DB_USER environment variable
    goto :eof
  )
) else set "USER=%~2"
if "%~3"=="" (
  set "PASS=%DB_PASS%"
) else set "PASS=%~3"
javac -cp .;mysql-connector-j-9.7.0.jar DbConnect.java
if errorlevel 1 goto :eof
java -cp .;mysql-connector-j-9.7.0.jar DbConnect "%URL%" "%USER%" "%PASS%"

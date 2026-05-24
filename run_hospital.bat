@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Could not find JDK at %JAVA_HOME%
  echo Update JAVA_HOME in run_hospital.bat to your installed JDK path.
  exit /b 1
)

set "LOCAL_MVN=%USERPROFILE%\.maven\maven-3.9.16\bin\mvn.cmd"
if not exist "%LOCAL_MVN%" (
  echo Could not find Maven at %LOCAL_MVN%
  echo Install Maven or update LOCAL_MVN in run_hospital.bat.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%USERPROFILE%\.maven\maven-3.9.16\bin;%PATH%"

set "MONGO_DATABASE=hospital_management"
if exist ".env" (
  for /f "usebackq tokens=1* delims==" %%A in (".env") do (
    if not "%%A"=="" (
      if /i "%%A"=="MONGO_URI" set "MONGO_URI=%%B"
      if /i "%%A"=="MONGO_DATABASE" set "MONGO_DATABASE=%%B"
      if /i "%%A"=="MONGO_PATIENTS_COLLECTION" set "MONGO_PATIENTS_COLLECTION=%%B"
      if /i "%%A"=="MONGO_DOCTORS_COLLECTION" set "MONGO_DOCTORS_COLLECTION=%%B"
      if /i "%%A"=="MONGO_APPOINTMENTS_COLLECTION" set "MONGO_APPOINTMENTS_COLLECTION=%%B"
    )
  )
)

if not defined MONGO_URI set "MONGO_URI=mongodb://localhost:27017"
if defined MONGO_URI (
  if /i "%MONGO_URI%"=="mongodb://localhost:27017" (
    echo MONGO_URI is not set. Using local MongoDB.
  ) else (
    echo Using MongoDB URI from .env or environment variable.
  )
)

call "%LOCAL_MVN%" -q -f "%~dp0pom.xml" -Dhospital.mongo.uri="%MONGO_URI%" -Dhospital.mongo.database="%MONGO_DATABASE%" clean javafx:run

endlocal

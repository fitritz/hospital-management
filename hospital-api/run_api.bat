@echo off
setlocal
cd /d "%~dp0"

if not defined JAVA_HOME (
  set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Could not find Java at %JAVA_HOME%
  echo Set JAVA_HOME to your JDK 17 path and run again.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

call mvnw.cmd spring-boot:run

endlocal

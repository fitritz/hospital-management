@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Could not find a valid JDK at %JAVA_HOME%
  echo Update run.bat with your installed JDK path.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

set "LOCAL_MVN=%USERPROFILE%\.maven\maven-3.9.16\bin\mvn.cmd"

if exist "%LOCAL_MVN%" (
	call "%LOCAL_MVN%" -q clean javafx:run
) else (
	echo Could not find Maven at %LOCAL_MVN%
	exit /b 1
)

endlocal

@echo off
setlocal
cd /d "%~dp0"
title ApexBank Server

echo ========================================
echo          APEX BANK - STARTUP
echo ========================================
echo.

where javac >nul 2>nul
if errorlevel 1 (
  echo Java compiler (javac) was not found.
  echo Please install a JDK, or ask me for the no-JDK alternative.
  echo.
  pause
  exit /b 1
)

if not exist out mkdir out

echo Compiling ApexBank...
javac -encoding UTF-8 -d out *.java
if errorlevel 1 (
  echo.
  echo Compilation failed. Please send me a screenshot of this window.
  pause
  exit /b 1
)

echo.
echo Starting the ApexBank web server...
echo Keep this window open while using the website.
echo Open this address in Chrome: http://localhost:8080
echo.
java -cp out Main 8080 web

echo.
echo ApexBank server stopped.
pause
endlocal

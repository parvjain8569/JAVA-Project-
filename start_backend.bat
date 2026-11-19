@echo off
title Bank Backend Server
color 0A
cls
echo ========================================================
echo       STARTING APEX BANK BACKEND & WEB SERVER
echo ========================================================
cd /d "%~dp0"
javac *.java
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b
)
echo Server is running on http://localhost:8080 ...
echo DO NOT CLOSE THIS WINDOW WHILE USING THE WEBSITE!
echo.
java Main
pause

@echo off
echo ===================================================
echo   BioPass Pro - Facial Attendance System Setup
echo ===================================================
echo.

docker --version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not installed or not running!
    echo Please install Docker Desktop and start it before running this setup.
    pause
    exit /b
)

echo [INFO] Docker detected!
echo [INFO] Building and starting the system natively...
echo.
docker-compose up --build

echo.
echo [INFO] Setup stopped.
pause

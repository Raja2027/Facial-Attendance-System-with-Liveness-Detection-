#!/bin/bash
echo "==================================================="
echo "  BioPass Pro - Facial Attendance System Setup"
echo "==================================================="
echo ""

if ! command -v docker &> /dev/null
then
    echo "[ERROR] Docker is not installed or not running!"
    echo "Please install Docker Desktop and start it before running this setup."
    exit 1
fi

echo "[INFO] Docker detected!"
echo "[INFO] Building and starting the system natively..."
echo ""
docker-compose up --build

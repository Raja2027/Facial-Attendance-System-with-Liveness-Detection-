---
title: Facial Ai Engine
emoji: 🏢
colorFrom: blue
colorTo: green
sdk: docker
pinned: false
app_port: 5001
---
# Facial Attendance System with Liveness Detection

A comprehensive, fully-dockerized Facial Attendance Management System built with a Spring Boot (Java) backend, a Flask (Python) AI engine, and MongoDB Atlas.

This system features enterprise-level AI capabilities:
- **FaceNet Integration:** High-accuracy facial feature extraction and embedding generation.
- **Liveness Detection (Anti-Spoofing):** MiniFASNet models ensure that users cannot bypass the system using photos or videos on a screen.
- **Dynamic Face Cropping:** RetinaFace detection automatically crops and aligns faces from live video feeds.

## Tech Stack
- **Frontend:** Vanilla HTML/JS with Tailwind CSS and Face-API.js
- **Backend:** Java 17, Spring Boot, MongoDB Vector Search
- **AI Engine:** Python 3.10, Flask, OpenCV, PyTorch, Keras
- **Orchestration:** Docker & Docker Compose

---

## 🚀 Quick Start Guide

Because this application relies on a complex stack of Java, Python, and C++ image processing libraries, it has been fully containerized. You do not need to install Python, Java, or OpenCV on your machine.

### Prerequisites
- **Docker Desktop** installed and running on your machine.
- **Git** installed.

### Installation & Execution

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Raja2027/Facial-Attendance-System-with-Liveness-Detection-.git
   cd Facial-Attendance-System-with-Liveness-Detection-
   ```

2. **Start the System:**
   You can start the entire system with a single click. Double-click the `setup.bat` file (for Windows) or run `setup.sh` (for Mac/Linux) in the root folder.
   
   *Alternatively, you can run the command manually:*
   ```bash
   docker-compose up --build
   ```
   *(Note: The first time you run this, it may take 5-10 minutes to download TensorFlow and compile the Docker images. Subsequent runs will be instant).*

3. **Open the Application:**
   Once the terminal shows both the `biopass-backend` and `biopass-ai` services are running, open your web browser and navigate to:
   
   👉 **http://localhost:8080**

## Project Structure
- `/attendanceSystem`: Contains the Spring Boot Backend and the HTML/JS Frontend UI.
- `/AI`: Contains the Flask server that exposes the `/train_face` and `/generate_embedding` API routes.
- `/antiSpoofing`: Contains the PyTorch models and utility scripts for detecting fake faces (photos/videos).

## Production Configuration

For Render backend deployment, configure these environment variables:

```bash
MONGODB_URI=your_mongodb_atlas_connection_string
AI_SERVER_URL=https://your-hugging-face-space-url
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_strong_admin_password
JWT_SECRET=your_long_random_secret_at_least_32_chars
JWT_TTL_MINUTES=480
RATE_LIMIT_ATTENDANCE_PER_MINUTE=30
RATE_LIMIT_REGISTER_PER_MINUTE=6
```

The backend now includes:
- JWT admin login at `/auth/login`
- protected `/persons/**` APIs
- request rate limiting for attendance and registration
- background registration jobs via `/persons/register-with-video-async`
- Spring Boot Actuator health/metrics endpoints
- AI service health checks through `/actuator/health`

For the Hugging Face AI service, expose the Flask app as before. The AI engine now also provides:
- `/health`
- structured request/error logs

## License
MIT License

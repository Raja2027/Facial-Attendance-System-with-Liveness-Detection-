FROM python:3.10-slim

# Install system dependencies required for OpenCV and face processing
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy requirements
COPY AI/requirements.txt ./AI/requirements.txt

# Pre-install torch CPU version to save massive amounts of space/time compared to CUDA version
RUN pip install --no-cache-dir torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

# Install the rest of the requirements with an increased timeout for large files like TensorFlow
RUN pip install --default-timeout=1000 --no-cache-dir -r AI/requirements.txt

# Copy the AI and antiSpoofing directories
# Note: The docker-compose context will be the root project folder
COPY AI/ ./AI/
COPY antiSpoofing/ ./antiSpoofing/

WORKDIR /app/AI
EXPOSE 5001

CMD ["python", "appp.py"]

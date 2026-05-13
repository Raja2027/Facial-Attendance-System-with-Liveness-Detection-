package com.example.attendancesystem.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "registration_jobs")
public class RegistrationJob {

    @Id
    private String id;

    private String status;
    private String name;
    private String message;
    private String studentId;
    private String registrationNumber;
    private Instant createdAt;
    private Instant updatedAt;

    public RegistrationJob() {
    }

    public RegistrationJob(String name) {
        this.name = name;
        this.status = "QUEUED";
        this.message = "Registration queued";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markProcessing() {
        this.status = "PROCESSING";
        this.message = "Face video is being processed";
        this.updatedAt = Instant.now();
    }

    public void markCompleted(Student student) {
        this.status = "COMPLETED";
        this.message = "Registration completed";
        this.studentId = student.getId();
        this.registrationNumber = student.getRegistrationNumber();
        this.updatedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = "FAILED";
        this.message = message;
        this.updatedAt = Instant.now();
    }
}

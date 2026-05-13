package com.example.attendancesystem.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "attendance_records")
public class AttendanceRecord {

    @Id
    private String id;

    private String personId;
    private String name;
    private String registrationNumber;
    private String type;
    private String status;
    private String message;
    private Instant markedAt;

    public AttendanceRecord() {
    }

    public AttendanceRecord(Student student, String status, String message) {
        this.personId = student.getId();
        this.name = student.getName();
        this.registrationNumber = student.getRegistrationNumber();
        this.type = student.getType();
        this.status = status;
        this.message = message;
        this.markedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getPersonId() {
        return personId;
    }

    public String getName() {
        return name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getMarkedAt() {
        return markedAt;
    }
}

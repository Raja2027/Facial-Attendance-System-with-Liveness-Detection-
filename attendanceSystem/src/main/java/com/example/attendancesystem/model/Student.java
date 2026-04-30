package com.example.attendancesystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "students")
public class Student {

    @Id
    private String id;

    private String name;
    private String mobile;
    private String email;
    private LocalDate dob;
    private String type;
    private String registrationNumber;

    private List<Double> embedding;   

    public Student() {
    }

    public Student(String name,
                   String mobile,
                   String email,
                   LocalDate dob,
                   String type,
                   String registrationNumber,
                   List<Double> embedding) {

        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.dob = dob;
        this.type = type;
        this.registrationNumber = registrationNumber;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getType() {
        return type;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}
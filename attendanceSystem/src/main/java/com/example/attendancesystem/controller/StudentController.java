package com.example.attendancesystem.controller;

import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.service.StudentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/persons")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping("/register-with-video")
    public Student registerWithVideo(
            @RequestParam String name,
            @RequestParam String mobile,
            @RequestParam String email,
            @RequestParam String dob,
            @RequestParam String type,
            @RequestParam("video") MultipartFile video
    ) throws Exception {

        return service.registerWithVideo(
                name,
                mobile,
                email,
                LocalDate.parse(dob),
                type,
                video
        );
    }
    @PostMapping("/mark-attendance")
    public Map<String, Object> markAttendance(
            @RequestParam("file") MultipartFile image
    ) throws Exception {

        return service.markAttendance(image);
    }

}
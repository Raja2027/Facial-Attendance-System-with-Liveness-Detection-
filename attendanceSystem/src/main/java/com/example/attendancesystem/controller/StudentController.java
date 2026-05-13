package com.example.attendancesystem.controller;

import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.model.RegistrationJob;
import com.example.attendancesystem.service.RegistrationJobService;
import com.example.attendancesystem.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/persons")
public class StudentController {

    private final StudentService service;
    private final RegistrationJobService registrationJobService;

    public StudentController(StudentService service,
                             RegistrationJobService registrationJobService) {
        this.service = service;
        this.registrationJobService = registrationJobService;
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

    @PostMapping("/register-with-video-async")
    public ResponseEntity<RegistrationJob> registerWithVideoAsync(
            @RequestParam String name,
            @RequestParam String mobile,
            @RequestParam String email,
            @RequestParam String dob,
            @RequestParam String type,
            @RequestParam("video") MultipartFile video
    ) throws Exception {

        RegistrationJob job = registrationJobService.enqueue(
                name,
                mobile,
                email,
                LocalDate.parse(dob),
                type,
                video
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    @GetMapping("/registration-jobs/{jobId}")
    public RegistrationJob registrationJob(@PathVariable String jobId) {
        return registrationJobService.getJob(jobId);
    }

    @PostMapping("/mark-attendance")
    public Map<String, Object> markAttendance(
            @RequestParam("file") MultipartFile image
    ) throws Exception {

        return service.markAttendance(image);
    }

}

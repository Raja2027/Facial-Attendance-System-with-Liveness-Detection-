package com.example.attendancesystem.service;

import java.time.LocalDate;

import com.example.attendancesystem.model.RegistrationJob;
import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.repository.RegistrationJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RegistrationJobWorker {

    private static final Logger log = LoggerFactory.getLogger(RegistrationJobWorker.class);

    private final RegistrationJobRepository registrationJobRepository;
    private final StudentService studentService;

    public RegistrationJobWorker(RegistrationJobRepository registrationJobRepository,
                                 StudentService studentService) {
        this.registrationJobRepository = registrationJobRepository;
        this.studentService = studentService;
    }

    @Async("registrationTaskExecutor")
    public void process(String jobId,
                        String name,
                        String mobile,
                        String email,
                        LocalDate dob,
                        String type,
                        byte[] videoBytes,
                        String filename) {
        RegistrationJob job = registrationJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Registration job not found"));

        try {
            job.markProcessing();
            registrationJobRepository.save(job);
            log.info("Registration job started jobId={} name={}", jobId, name);

            Student student = studentService.registerWithVideoBytes(
                    name, mobile, email, dob, type, videoBytes, filename
            );

            job.markCompleted(student);
            registrationJobRepository.save(job);
            log.info("Registration job completed jobId={} regNo={}",
                    jobId, student.getRegistrationNumber());
        } catch (Exception exception) {
            job.markFailed(exception.getMessage());
            registrationJobRepository.save(job);
            log.error("Registration job failed jobId={} name={}", jobId, name, exception);
        }
    }
}

package com.example.attendancesystem.service;

import java.time.LocalDate;

import com.example.attendancesystem.model.RegistrationJob;
import com.example.attendancesystem.repository.RegistrationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RegistrationJobService {

    private final RegistrationJobRepository registrationJobRepository;
    private final RegistrationJobWorker registrationJobWorker;

    public RegistrationJobService(RegistrationJobRepository registrationJobRepository,
                                  RegistrationJobWorker registrationJobWorker) {
        this.registrationJobRepository = registrationJobRepository;
        this.registrationJobWorker = registrationJobWorker;
    }

    public RegistrationJob enqueue(String name,
                                   String mobile,
                                   String email,
                                   LocalDate dob,
                                   String type,
                                   MultipartFile video) throws Exception {
        RegistrationJob job = registrationJobRepository.save(new RegistrationJob(name));

        registrationJobWorker.process(
                job.getId(),
                name,
                mobile,
                email,
                dob,
                type,
                video.getBytes(),
                video.getOriginalFilename()
        );

        return job;
    }

    public RegistrationJob getJob(String jobId) {
        return registrationJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Registration job not found"));
    }
}

package com.example.attendancesystem.service;

import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.repository.StudentRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final SequenceGeneratorService sequenceGenerator;

    public StudentService(StudentRepository repository,
                          SequenceGeneratorService sequenceGenerator) {
        this.repository = repository;
        this.sequenceGenerator = sequenceGenerator;
    }

    public Map<String, Object> markAttendance(MultipartFile image) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "http://localhost:5001/generate_embedding",
                        request,
                        Map.class
                );

        if (response.getBody() == null ||
                response.getBody().containsKey("error")) {
            throw new RuntimeException("Face not detected");
        }

        @SuppressWarnings("unchecked")
        List<Double> embedding =
                (List<Double>) response.getBody().get("embedding");

        System.out.println("Embedding size: " + embedding.size());
        System.out.println("First 5 values: " + embedding.subList(0, 5));

        // 🔥 Mongo Vector Search
        Student matchedStudent = repository.findBestMatch(embedding);

        if (matchedStudent == null) {
            throw new RuntimeException("No match found");
        }

        return Map.of(
                "status", "success",
                "name", matchedStudent.getName(),
                "regNo", matchedStudent.getRegistrationNumber()
        );
    }

    public Student registerWithVideo(String name,
                                     String mobile,
                                     String email,
                                     LocalDate dob,
                                     String type,
                                     MultipartFile video) throws Exception {

        // Generate Registration Number
        int year = Year.now().getValue();
        String prefix = type.equalsIgnoreCase("student") ? "STD" : "FAC";
        long sequence = sequenceGenerator.generateSequence(prefix + "_sequence_" + year);
        String regNo = String.format("%s%d%04d", prefix, year, sequence);

        // Call Python Server
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(video.getBytes()) {
            @Override
            public String getFilename() {
                return video.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "http://localhost:5001/train_face",
                        requestEntity,
                        Map.class
                );

        if (response.getBody() == null ||
                response.getBody().containsKey("error")) {
            throw new RuntimeException("Face not detected in backend");
        }

        @SuppressWarnings("unchecked")
        List<Double> embedding =
                (List<Double>) response.getBody().get("embedding");

        // Save to MongoDB
        Student person = new Student(
                name,
                mobile,
                email,
                dob,
                type,
                regNo,
                embedding
        );

        return repository.save(person);
    }
}
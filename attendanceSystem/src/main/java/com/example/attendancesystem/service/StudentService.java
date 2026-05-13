package com.example.attendancesystem.service;

import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    @Value("${ai.server.url:http://localhost:5001}")
    private String aiServerUrl;

    private final StudentRepository repository;
    private final SequenceGeneratorService sequenceGenerator;
    private final RestTemplate restTemplate;
    private final AttendanceCacheService attendanceCacheService;

    public StudentService(StudentRepository repository,
                          SequenceGeneratorService sequenceGenerator,
                          RestTemplateBuilder restTemplateBuilder,
                          AttendanceCacheService attendanceCacheService) {
        this.repository = repository;
        this.sequenceGenerator = sequenceGenerator;
        this.attendanceCacheService = attendanceCacheService;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(120))
                .build();
    }

    public Map<String, Object> markAttendance(MultipartFile image) throws Exception {
        byte[] imageBytes = image.getBytes();
        String cacheKey = sha256(imageBytes);
        Optional<Map<String, Object>> cachedResult = attendanceCacheService.get(cacheKey);
        if (cachedResult.isPresent()) {
            log.info("Attendance cache hit imageHash={}", cacheKey);
            return cachedResult.get();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        aiServerUrl + "/generate_embedding",
                        request,
                        Map.class
                );

        if (response.getBody() == null ||
                response.getBody().containsKey("error")) {
            log.warn("Attendance embedding failed aiResponse={}", response.getBody());
            throw new RuntimeException("Face not detected");
        }

        @SuppressWarnings("unchecked")
        List<Double> embedding =
                (List<Double>) response.getBody().get("embedding");

        log.info("Attendance embedding generated dimensions={}", embedding.size());

        Student matchedStudent = repository.findBestMatch(embedding);

        if (matchedStudent == null) {
            log.warn("Attendance match failed: no student matched embedding");
            throw new RuntimeException("No match found");
        }

        log.info("Attendance matched name={} regNo={}",
                matchedStudent.getName(), matchedStudent.getRegistrationNumber());

        Map<String, Object> result = Map.of(
                "status", "success",
                "name", matchedStudent.getName(),
                "regNo", matchedStudent.getRegistrationNumber()
        );
        attendanceCacheService.put(cacheKey, result);
        return result;
    }

    public Student registerWithVideo(String name,
                                     String mobile,
                                     String email,
                                     LocalDate dob,
                                     String type,
                                     MultipartFile video) throws Exception {
        return registerWithVideoBytes(
                name,
                mobile,
                email,
                dob,
                type,
                video.getBytes(),
                video.getOriginalFilename()
        );
    }

    public Student registerWithVideoBytes(String name,
                                          String mobile,
                                          String email,
                                          LocalDate dob,
                                          String type,
                                          byte[] videoBytes,
                                          String filename) throws Exception {

        int year = Year.now().getValue();
        String prefix = type.equalsIgnoreCase("student") ? "STD" : "FAC";
        long sequence = sequenceGenerator.generateSequence(prefix + "_sequence_" + year);
        String regNo = String.format("%s%d%04d", prefix, year, sequence);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(videoBytes) {
            @Override
            public String getFilename() {
                return filename == null || filename.isBlank() ? "face-video.webm" : filename;
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        aiServerUrl + "/train_face",
                        requestEntity,
                        Map.class
                );

        if (response.getBody() == null ||
                response.getBody().containsKey("error")) {
            log.warn("Registration embedding failed name={} aiResponse={}", name, response.getBody());
            throw new RuntimeException("Face not detected in backend");
        }

        @SuppressWarnings("unchecked")
        List<Double> embedding =
                (List<Double>) response.getBody().get("embedding");

        Student person = new Student(
                name,
                mobile,
                email,
                dob,
                type,
                regNo,
                embedding
        );

        Student saved = repository.save(person);
        log.info("Registration completed name={} regNo={} type={}",
                saved.getName(), saved.getRegistrationNumber(), saved.getType());
        return saved;
    }

    private String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}

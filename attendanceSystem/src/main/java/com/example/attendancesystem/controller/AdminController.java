package com.example.attendancesystem.controller;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.attendancesystem.model.AttendanceRecord;
import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.repository.AttendanceRecordRepository;
import com.example.attendancesystem.repository.StudentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public AdminController(StudentRepository studentRepository,
                           AttendanceRecordRepository attendanceRecordRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return Map.of(
                "totalPeople", studentRepository.count(),
                "totalStudents", studentRepository.countByTypeIgnoreCase("student"),
                "totalTeachers", studentRepository.countByTypeIgnoreCase("faculty"),
                "recentAttendance", attendanceRecordRepository.findTop50ByOrderByMarkedAtDesc()
        );
    }

    @GetMapping("/people")
    public List<Map<String, Object>> people() {
        return studentRepository.findTop100ByOrderByRegistrationNumberAsc()
                .stream()
                .map(this::personRow)
                .toList();
    }

    @GetMapping("/attendance")
    public List<AttendanceRecord> attendance(@RequestParam(required = false) String type) {
        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) {
            return attendanceRecordRepository.findTop50ByOrderByMarkedAtDesc();
        }
        return attendanceRecordRepository.findTop50ByTypeIgnoreCaseOrderByMarkedAtDesc(type);
    }

    private Map<String, Object> personRow(Student student) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", student.getId());
        row.put("name", student.getName());
        row.put("email", student.getEmail());
        row.put("type", student.getType());
        row.put("registrationNumber", student.getRegistrationNumber());
        return row;
    }
}

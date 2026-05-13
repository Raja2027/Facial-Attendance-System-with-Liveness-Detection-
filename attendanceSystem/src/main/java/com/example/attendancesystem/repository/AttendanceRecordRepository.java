package com.example.attendancesystem.repository;

import java.util.List;

import com.example.attendancesystem.model.AttendanceRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRecordRepository extends MongoRepository<AttendanceRecord, String> {

    List<AttendanceRecord> findTop50ByOrderByMarkedAtDesc();

    List<AttendanceRecord> findTop50ByTypeIgnoreCaseOrderByMarkedAtDesc(String type);
}

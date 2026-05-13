package com.example.attendancesystem.repository;

import com.example.attendancesystem.model.RegistrationJob;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RegistrationJobRepository extends MongoRepository<RegistrationJob, String> {
}

package com.example.attendancesystem.repository;

import com.example.attendancesystem.model.Student;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {
    @Aggregation(pipeline = {
            "{ $vectorSearch: { " +
                    "index: 'vector_index', " +
                    "path: 'embedding', " +
                    "queryVector: ?0, " +
                    "numCandidates: 100, " +
                    "limit: 1 } }"
    })
    Student findBestMatch(List<Double> embedding);

    long countByTypeIgnoreCase(String type);

    List<Student> findTop100ByOrderByRegistrationNumberAsc();
}

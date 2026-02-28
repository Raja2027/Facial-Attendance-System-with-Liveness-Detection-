package com.example.attendancesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.example.attendancesystem.model.Sequence;

@Service
public class SequenceGeneratorService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public long generateSequence(String seqName) {

        Query query = new Query(Criteria.where("_id").is(seqName));
        Update update = new Update().inc("seq", 1);

        FindAndModifyOptions options =
                new FindAndModifyOptions().returnNew(true).upsert(true);

        Sequence sequence = mongoTemplate.findAndModify(
                query, update, options, Sequence.class);

        return sequence.getSeq();
    }
}
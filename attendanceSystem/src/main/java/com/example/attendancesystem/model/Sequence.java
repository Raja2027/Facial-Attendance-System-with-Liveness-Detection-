package com.example.attendancesystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequences")
public class Sequence {

    @Id
    private String id;

    private long seq;

    public Sequence() {}

    public Sequence(String id, long seq) {
        this.id = id;
        this.seq = seq;
    }

    public String getId() { return id; }

    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
}
package com.giftAndGo.assignment.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Entry {
    private String uuid;
    private String id;
    private String name;
    private String likes;   
    private String transport;
    private double avgSpeed;
    private double topSpeed;
}


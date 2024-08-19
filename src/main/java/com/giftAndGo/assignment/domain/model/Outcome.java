package com.giftAndGo.assignment.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
public class Outcome {
    private String name;
    private String transport;
    private double topSpeed;
}


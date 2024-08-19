package com.giftAndGo.assignment.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Getter
public class FileProcessingRequest {

    public FileProcessingRequest(){
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String uri;

    private LocalDateTime timestamp;

    private int responseCode;

    private String ipAddress;

    private String countryCode;

    private String ipProvider;

    private long timeLapsed;
}


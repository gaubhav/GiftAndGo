package com.giftAndGo.assignment.domain.service;

import com.giftAndGo.assignment.adapter.util.FileParser;
import com.giftAndGo.assignment.configuration.AppConfig;
import com.giftAndGo.assignment.domain.exception.InputProcessingException;
import com.giftAndGo.assignment.domain.model.Entry;
import com.giftAndGo.assignment.domain.model.Outcome;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FileProcessingService {

    private final FileParser fileParser;
    private final AppConfig appConfig;

    public List<Outcome> processFile(MultipartFile file) {

        List<Entry> entries;
        try {
            entries = fileParser.parseFile(file.getBytes(), appConfig.getFeature().isSkipValidation());
        } catch(IOException e) {
            throw new InputProcessingException("Unable to read the input file", e);
        }
        return entries.stream()
                .map(entry -> Outcome.builder().name(entry.getName()).transport(entry.getTransport()).topSpeed(entry.getTopSpeed()).build())
                .collect(Collectors.toList());
    }
}


package com.giftAndGo.assignment.adapter.util;

import com.giftAndGo.assignment.domain.exception.InvalidInputException;
import com.giftAndGo.assignment.domain.model.Entry;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class FileParser {
    public List<Entry> parseFile(byte[] fileContent, boolean skipValidation) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContent)))) {
            return reader.lines()
                    .map(line -> line.split("\\|"))
                    .peek(parts -> validate(parts, skipValidation))
                    .map(parts ->
                        Entry.builder()
                                .uuid(parts[0])
                                .id(parts[1])
                                .name(parts[2])
                                .likes(parts[3])
                                .transport(parts[4])
                                .avgSpeed(Double.parseDouble(parts[5]))
                                .topSpeed(Double.parseDouble(parts[6]))
                                .build()
                    )
                    .collect(Collectors.toList());
        }
    }


    private void validate(String[] parts, boolean skipValidation) {
        if(skipValidation){
            return;
        }

        if (parts.length != 7) {
            throw new InvalidInputException("Invalid file format: Expected 7 parts, got " + parts.length);
        }

        List<String> errorMessages = new ArrayList<>();

        validatePart(parts[0], "Invalid UUID format", this::isValidUUID, errorMessages);
        validatePart(parts[1], "ID cannot be empty", this::isNotEmpty, errorMessages);
        validatePart(parts[2], "Name cannot be empty", this::isNotEmpty, errorMessages);
        validatePart(parts[3], "Likes cannot be empty", this::isNotEmpty, errorMessages);
        validatePart(parts[4], "Transport cannot be empty", this::isNotEmpty, errorMessages);
        validatePart(parts[5], "Invalid average speed", this::isValidSpeed, errorMessages);
        validatePart(parts[6], "Invalid top speed", this::isValidSpeed, errorMessages);

        if (!errorMessages.isEmpty()) {
            throw new InvalidInputException("Validation failed for line: "  + String.join("|",parts) + String.join(" ; ", errorMessages));
        }
    }

    private void validatePart(String part, String errorMessage, Predicate<String> validator, List<String> errorMessages) {
        if (Optional.ofNullable(part).filter(validator).isEmpty()) {
            errorMessages.add(errorMessage);
        }
    }

    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isNotEmpty(String value) {
        return !value.trim().isEmpty();
    }

    private boolean isValidSpeed(String value) {
        try {
            double parsedValue = Double.parseDouble(value);
            return parsedValue >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}


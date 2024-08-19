package com.giftAndGo.assignment.web.mapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;

public class ResponseMapper {

    public static Resource mapToResourceFrom(String fileContent) throws IOException {

        Path filePath = Paths.get("files").resolve("OutcomeFile.json").normalize();
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileContent.getBytes());

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("File not found " + filePath);
        }

        return resource;
    }

}

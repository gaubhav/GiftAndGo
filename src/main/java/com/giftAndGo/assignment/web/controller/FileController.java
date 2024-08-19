package com.giftAndGo.assignment.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftAndGo.assignment.configuration.AppConfig;
import com.giftAndGo.assignment.domain.model.Outcome;
import com.giftAndGo.assignment.domain.service.FileProcessingService;
import com.giftAndGo.assignment.domain.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.giftAndGo.assignment.web.mapper.ResponseMapper.mapToResourceFrom;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FileController {

    private final FileProcessingService fileProcessingService;

    private final ObjectMapper objectMapper;
    private final AppConfig appConfig;

    private final LocationService locationService;

    @PostMapping("/process")
    public ResponseEntity<Resource> processFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException{
        String clientIp = getClientIp(request);

        AppConfig.IpValidationProperties ipValidationProperties = appConfig.getIpValidationProperties();
        locationService.validateIpAddress(clientIp, ipValidationProperties.getBlockedCountries(), ipValidationProperties.getBlockedIsps());

        List<Outcome> outcomes = fileProcessingService.processFile(file);

        Resource resource = mapToResourceFrom(objectMapper.writeValueAsString(outcomes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}


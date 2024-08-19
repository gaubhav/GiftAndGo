package com.giftAndGo.assignment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftAndGo.assignment.adapter.repository.FileProcessingRequestLogRepository;
import com.giftAndGo.assignment.configuration.AppConfig;
import com.giftAndGo.assignment.domain.exception.InputProcessingException;
import com.giftAndGo.assignment.domain.exception.InvalidInputException;
import com.giftAndGo.assignment.domain.exception.IpBlockedException;
import com.giftAndGo.assignment.domain.model.Outcome;
import com.giftAndGo.assignment.domain.service.FileProcessingService;
import com.giftAndGo.assignment.domain.service.LocationService;
import com.giftAndGo.assignment.web.controller.FileController;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({FileController.class, RequestContext.class, AppConfig.class})
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileProcessingService fileProcessingService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private FileProcessingRequestLogRepository requestLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void testProcessFile_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        Outcome outcome1 = Outcome.builder().name("John Smith").transport("Rides A Bike").topSpeed(12.1).build();
        Outcome outcome2 = Outcome.builder().name("Mike Smith").transport("Drives an SUV").topSpeed(95.5).build();
        List<Outcome> outcomes = List.of(outcome1,outcome2);

        when(fileProcessingService.processFile(any())).thenReturn(outcomes);

        String expectedJson = objectMapper.writeValueAsString(outcomes);

        mockMvc.perform(multipart("/api/files/process")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().json(expectedJson));
    }

    @Test
    @SneakyThrows
    public void testProcessFile_HandleInputProcessingException() {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(fileProcessingService.processFile(any())).thenThrow(new InputProcessingException("Unable to parse the file"));

        mockMvc.perform(multipart("/api/files/process")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"Unable to parse the file\"}", true));
    }

    @Test
    @SneakyThrows
    public void testProcessFile_HandleInvalidInputException() {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(fileProcessingService.processFile(any())).thenThrow(new InvalidInputException("Fields are not in correct format"));

        mockMvc.perform(multipart("/api/files/process")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\":\"Fields are not in correct format\"}", true));
    }

    @Test
    @SneakyThrows
    public void testProcessFile_HandleIPException() {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        doThrow(new IpBlockedException("Ip Blocked for some reason"))
                .when(locationService).validateIpAddress(anyString(),anyList(),anyList());

        mockMvc.perform(multipart("/api/files/process")
                        .file(file))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"message\":\"Ip Blocked for some reason\"}", true));
    }

    @Test
    @SneakyThrows
    public void testProcessFile_HandleUnknownException() {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(fileProcessingService.processFile(any())).thenThrow(new RuntimeException("Unknown Exception"));

        mockMvc.perform(multipart("/api/files/process")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"There was an unexpected error while processing your request\"}", true));
    }
}


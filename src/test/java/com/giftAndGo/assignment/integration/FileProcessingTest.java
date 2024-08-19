package com.giftAndGo.assignment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftAndGo.assignment.adapter.repository.FileProcessingRequestLogRepository;
import com.giftAndGo.assignment.domain.model.FileProcessingRequest;
import com.giftAndGo.assignment.domain.model.Outcome;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.giftAndGo.assignment.fixtures.Fixtures.createOutcome;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FileProcessingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileProcessingRequestLogRepository repository;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());

        stubFor(get(urlPathMatching("/json/24.48.0.0"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\",\"countryCode\":\"GB\",\"isp\":\"SomeISP\"}")));

        stubFor(get(urlPathMatching("/json/1.0.3.255"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\",\"countryCode\":\"CN\",\"isp\":\"SomeISP\"}")));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @SneakyThrows
    public void testProcessFileEndpoint_Success() {
        String fileContent = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes());

        Outcome expectedOutcome1 = createOutcome(12.1, "John Smith", "Rides A Bike").build();
        Outcome expectedOutcome2 = createOutcome(95.5, "Mike Smith", "Drives an SUV").build();

        String expectedJson = objectMapper.writeValueAsString(List.of(expectedOutcome1, expectedOutcome2));

        mockMvc.perform(multipart("/api/files/process")
                        .file(file)
                        .header("X-FORWARDED-FOR", "24.48.0.0"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().json(expectedJson));

        List<FileProcessingRequest> logs = repository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResponseCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @SneakyThrows
    public void testProcessFileEndpoint_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/api/files/process")
                        .file(file)
                        .header("X-FORWARDED-FOR", "24.48.0.0"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().json("[]"));

        List<FileProcessingRequest> logs = repository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResponseCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @SneakyThrows
    public void testProcessFileEndpoint_ThrowsValidationException() {
        String fileContent = "invalid-uuid|||||invalid|-1.0\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes());

        mockMvc.perform(multipart("/api/files/process")
                        .file(file)
                        .header("X-FORWARDED-FOR", "24.48.0.0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"message\":\"Validation failed for line: invalid-uuid|||||invalid|-1.0Invalid UUID format ; ID cannot be empty ; Name cannot be empty ; Likes cannot be empty ; Transport cannot be empty ; Invalid average speed ; Invalid top speed\"}"));

        List<FileProcessingRequest> logs = repository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResponseCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @SneakyThrows
    public void testProcessFileEndpoint_ThrowsIpBlockedException() {
        String fileContent = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes());

        mockMvc.perform(multipart("/api/files/process")
                        .file(file)
                        .header("X-FORWARDED-FOR", "1.0.3.255"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"message\":\"Access denied for IP address: 1.0.3.255\"}"));

        List<FileProcessingRequest> logs = repository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResponseCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}

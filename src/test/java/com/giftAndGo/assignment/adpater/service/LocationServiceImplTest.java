package com.giftAndGo.assignment.adpater.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftAndGo.assignment.adapter.model.LocationApiResponse;
import com.giftAndGo.assignment.adapter.service.LocationServiceImpl;
import com.giftAndGo.assignment.domain.exception.IpBlockedException;
import com.giftAndGo.assignment.web.RequestContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.giftAndGo.assignment.fixtures.Fixtures.createLocationApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RequestContext requestContext;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Mock
    private HttpResponse<String> httpResponse;


    @Test
    @SneakyThrows
    void testValidateIpAddress_Success()  {
        
        String ipAddress = "1.0.0.0";
        List<String> blockedCountries = List.of("CN", "ES", "US");
        List<String> blockedIsps = List.of("Amazon", "Google", "Microsoft");

        LocationApiResponse mockResponse =  createLocationApiResponse("success", "GB", "someISP").build();

        String mockResponseBody = """
            {
                "status": "success",
                "countryCode": "GB",
                "isp": "someISP"
            }
            """;

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(objectMapper.readValue(anyString(), eq(LocationApiResponse.class))).thenReturn(mockResponse);

        assertDoesNotThrow(() -> locationService.validateIpAddress(ipAddress, blockedCountries, blockedIsps));
    }

    @Test
    @SneakyThrows
    void testValidateIpAddress_InvalidIp()  {
        IpBlockedException exception = assertThrows(IpBlockedException.class,() -> locationService.validateIpAddress(null, List.of(), List.of()));
        assertEquals(
                "Unable to validate IP address", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void testValidateIpAddress_BlockedCountry()  {
        String ipAddress = "1.0.0.0";
        List<String> blockedCountries = List.of("AA", "BB", "CC");

        LocationApiResponse mockResponse = createLocationApiResponse("success","BB","someISP").build();

        String mockResponseBody = """
            {
                "status": "success",
                "countryCode": "BB",
                "isp": "someISP"
            }
            """;

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(objectMapper.readValue(anyString(), eq(LocationApiResponse.class))).thenReturn(mockResponse);

        IpBlockedException exception = assertThrows(IpBlockedException.class, () -> locationService.validateIpAddress(ipAddress, blockedCountries, List.of()));
        assertEquals(
                "Access denied for IP address: 1.0.0.0", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void testValidateIpAddress_BlockedIsp()  {
        String ipAddress = "1.0.0.0";
        List<String> blockedIsps = List.of("ISP1", "ISP2", "ISP3");

        LocationApiResponse mockResponse = createLocationApiResponse("success","someCountry","ISP3").build();

        String mockResponseBody = """
            {
                "status": "success",
                "countryCode": "someCountry",
                "isp": "ISP3"
            }
            """;

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(objectMapper.readValue(anyString(), eq(LocationApiResponse.class))).thenReturn(mockResponse);

        IpBlockedException exception = assertThrows(IpBlockedException.class, () -> locationService.validateIpAddress(ipAddress, List.of(), blockedIsps));
        assertEquals("Access denied for IP address: 1.0.0.0", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void testValidateIpAddress_FailedResponse()  {
        String ipAddress = "127.0.0.1";

        LocationApiResponse mockResponse = createLocationApiResponse("fail", null, null).build();

        String mockResponseBody = """
            {
                "status": "fail"
            }
            """;

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(objectMapper.readValue(anyString(), eq(LocationApiResponse.class))).thenReturn(mockResponse);

        IpBlockedException exception = assertThrows(IpBlockedException.class, () -> locationService.validateIpAddress(ipAddress, List.of(), List.of()));
        assertEquals("Unable to validate IP address: 127.0.0.1", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void testValidateIpAddress_IOException()  {
        String ipAddress = "1.0.0.0";

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException());

        IpBlockedException exception = assertThrows(IpBlockedException.class, () -> locationService.validateIpAddress(ipAddress, List.of(), List.of()));
        assertEquals("Failed to validate IP address: 1.0.0.0", exception.getMessage());
    }
}


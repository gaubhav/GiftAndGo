package com.giftAndGo.assignment.adapter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftAndGo.assignment.adapter.model.LocationApiResponse;
import com.giftAndGo.assignment.domain.exception.IpBlockedException;
import com.giftAndGo.assignment.domain.service.LocationService;
import com.giftAndGo.assignment.web.RequestContext;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@AllArgsConstructor
@Log4j2
public class LocationServiceImpl implements LocationService {

    private static final String API_URL = "http://ip-api.com/json/";

    private static final String STATUS_FAIL = "fail";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final RequestContext requestContext;

    @Override
    public void validateIpAddress(String ipAddress, List<String> blockedCountries, List<String> blockedIsps) {
        if (ipAddress == null) {
            throw new IpBlockedException("Unable to validate IP address");
        }

        try {
            // I haven't added "?fields=status,countryCode,isp"; to the request since it seemed like an unnecessary
            // detail to save on some data transfer. I am happy to be persuaded otherwise.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + ipAddress))
                    .GET()
                    .build();

            log.debug("Request to location service: " + request);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Response from location service: " + response);

            LocationApiResponse locationApiResponse = objectMapper.readValue(response.body(), LocationApiResponse.class);
            storeRequestDetailsForAudit(ipAddress, locationApiResponse);

            if (STATUS_FAIL.equalsIgnoreCase(locationApiResponse.getStatus())) {
                throw new IpBlockedException("Unable to validate IP address: " + ipAddress);
            }

            if (isBlockedCountry(locationApiResponse.getCountryCode(), blockedCountries)
                    || isBlockedIsp(locationApiResponse.getIsp(), blockedIsps)) {
                // I have a generic error message for either case (blocked country / ISP) for security reasons.
                throw new IpBlockedException("Access denied for IP address: " + ipAddress);
            }
        } catch (IOException | InterruptedException e) {
            throw new IpBlockedException("Failed to validate IP address: " + ipAddress);
        }
    }

    private void storeRequestDetailsForAudit(String ipAddress, LocationApiResponse locationApiResponse) {
        requestContext.setIpAddress(ipAddress);
        requestContext.setIsp(locationApiResponse.getIsp());
        requestContext.setCountryCode(locationApiResponse.getCountryCode());
    }

    private boolean isBlockedCountry(String country, List<String> blockedCountries) {
        return blockedCountries.stream().anyMatch(country::equals);
    }

    private boolean isBlockedIsp(String isp, List<String> blockedIsps) {
        return blockedIsps.stream().anyMatch(isp::equals);
    }
}

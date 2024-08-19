package com.giftAndGo.assignment.adapter.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class LocationApiResponse {
    private String status;
    private String countryCode;
    private String isp;
}

package com.giftAndGo.assignment.web.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class ErrorResponse {
    private String message;
}

package com.giftAndGo.assignment.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class AppConfig {

    private FeatureProperties feature;
    private IpValidationProperties ipValidationProperties;

    @Data
    public static class FeatureProperties {
        private boolean skipValidation;
    }

    @Data
    public static class IpValidationProperties {
        private List<String> blockedCountries;
        private List<String> blockedIsps;
    }
}

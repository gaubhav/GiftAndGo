package com.giftAndGo.assignment.domain.service;

import com.giftAndGo.assignment.domain.exception.IpBlockedException;

import java.util.List;

public interface LocationService {
    void validateIpAddress(String ipAddress, List<String> blockedCountries, List<String> blockedIsps) throws IpBlockedException;
}

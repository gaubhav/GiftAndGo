package com.giftAndGo.assignment.fixtures;

import com.giftAndGo.assignment.adapter.model.LocationApiResponse;
import com.giftAndGo.assignment.domain.model.Entry;
import com.giftAndGo.assignment.domain.model.Outcome;

import java.util.UUID;

public class Fixtures {
    public static Entry.EntryBuilder createEntry(String id, String likes, double avgSpeed, double topSpeed, String name, String transport){
        return Entry.builder()
                .uuid(UUID.randomUUID().toString())
                .id(id)
                .likes(likes)
                .avgSpeed(avgSpeed)
                .topSpeed(topSpeed)
                .name(name)
                .transport(transport);
    }

    public static Outcome.OutcomeBuilder createOutcome(double topSpeed, String name, String transport){
        return Outcome.builder()
                .topSpeed(topSpeed)
                .transport(transport)
                .name(name);
    }

    public static LocationApiResponse.LocationApiResponseBuilder createLocationApiResponse(String status, String countryCode, String isp){
        return LocationApiResponse.builder()
                .status(status)
                .countryCode(countryCode)
                .isp(isp);
    }
}

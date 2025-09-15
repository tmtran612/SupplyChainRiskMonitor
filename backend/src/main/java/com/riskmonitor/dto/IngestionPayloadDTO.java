package com.riskmonitor.dto;

import java.util.List;

public class IngestionPayloadDTO {
    private String source;
    private List<EventPayloadDTO> events;

    // Getters and Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<EventPayloadDTO> getEvents() {
        return events;
    }

    public void setEvents(List<EventPayloadDTO> events) {
        this.events = events;
    }
}

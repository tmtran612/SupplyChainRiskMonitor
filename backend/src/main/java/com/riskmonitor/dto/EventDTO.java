package com.riskmonitor.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventDTO {
    private UUID id;
    private String supplierName;
    private LocalDateTime timestamp;
    private String type;
    private String payload;

    public EventDTO(UUID id, String supplierName, LocalDateTime timestamp, String type, String payload) {
        this.id = id;
        this.supplierName = supplierName;
        this.timestamp = timestamp;
        this.type = type;
        this.payload = payload;
    }

    // Getters
    public UUID getId() { return id; }
    public String getSupplierName() { return supplierName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getPayload() { return payload; }
}

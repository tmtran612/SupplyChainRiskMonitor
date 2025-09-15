package com.riskmonitor.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlertDTO {
    private UUID id;
    private String supplierName;
    private LocalDateTime timestamp;
    private String message;
    private String severity;
    private String supplierCurrentRiskLevel;

    public AlertDTO(UUID id, String supplierName, LocalDateTime timestamp, String message, String severity, String supplierCurrentRiskLevel) {
        this.id = id;
        this.supplierName = supplierName;
        this.timestamp = timestamp;
        this.message = message;
        this.severity = severity;
        this.supplierCurrentRiskLevel = supplierCurrentRiskLevel;
    }

    // Getters
    public UUID getId() { return id; }
    public String getSupplierName() { return supplierName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getSupplierCurrentRiskLevel() { return supplierCurrentRiskLevel; }
}

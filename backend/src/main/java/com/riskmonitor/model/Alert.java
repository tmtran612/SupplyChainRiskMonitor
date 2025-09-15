package com.riskmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Alert {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private LocalDateTime timestamp;
    private String message;
    private String severity;
    private boolean acknowledged = false;
    private boolean active = true; // can be toggled off if resolved

    public Alert() {}

    public Alert(UUID id, Supplier supplier, LocalDateTime timestamp, String message, String severity) {
        this.id = id;
        this.supplier = supplier;
        this.timestamp = timestamp;
        this.message = message;
        this.severity = severity;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

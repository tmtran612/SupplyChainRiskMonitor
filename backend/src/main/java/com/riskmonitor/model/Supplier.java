package com.riskmonitor.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
// Align with existing database table name 'suppliers' and UUID primary key
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    // Primary canonical location column (new)
    @Column(name = "location")
    private String location;
    // Legacy column 'region' may still exist in DB with data. Map it and fallback.
    @Column(name = "region")
    private String legacyRegion;
    @Column(name = "tier")
    private Integer tier; // Added tier
    @Column(name = "industry")
    private String industry; // Added industry
    @Column(name = "contact_email")
    private String contactEmail;
    @Column(name = "contact_phone")
    private String contactPhone;
    @Column(name = "baseline_risk")
    private Double baselineRisk;
    private String lastUpdate;
    private Double latitude; // nullable until backfilled
    private Double longitude; // nullable until backfilled

    public Supplier() {}

    public Supplier(UUID id, String name, String location, Integer tier, String industry, Double baselineRisk, String lastUpdate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.tier = tier;
        this.industry = industry;
        this.baselineRisk = baselineRisk;
        this.lastUpdate = lastUpdate;
    }

    public Supplier(UUID id, String name, String location, Integer tier, String industry, double baselineRisk, String lastUpdate, Double latitude, Double longitude) {
        this(id, name, location, tier, industry, baselineRisk, lastUpdate);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Supplier(UUID id, String name, String location, Integer tier, String industry, String contactEmail, String contactPhone, Double baselineRisk, String lastUpdate, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.tier = tier;
        this.industry = industry;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.baselineRisk = baselineRisk;
        this.lastUpdate = lastUpdate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() {
        return (location != null && !location.isEmpty()) ? location : legacyRegion;
    }
    public void setLocation(String location) { this.location = location; }
    public String getLegacyRegion() { return legacyRegion; }
    public void setLegacyRegion(String legacyRegion) { this.legacyRegion = legacyRegion; }
    public Integer getTier() { return tier; }
    public void setTier(Integer tier) { this.tier = tier; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Double getBaselineRisk() { return baselineRisk; }
    public void setBaselineRisk(Double baselineRisk) { this.baselineRisk = baselineRisk; }
    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}

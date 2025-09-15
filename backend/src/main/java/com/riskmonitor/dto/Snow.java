package com.riskmonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Snow {

    @JsonProperty("1h")
    private Double oneHour;

    // Getters and setters
    public Double getOneHour() {
        return oneHour;
    }

    public void setOneHour(Double oneHour) {
        this.oneHour = oneHour;
    }
}

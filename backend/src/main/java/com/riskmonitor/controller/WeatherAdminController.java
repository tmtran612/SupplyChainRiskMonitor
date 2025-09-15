package com.riskmonitor.controller;

import com.riskmonitor.service.ExternalDataFetcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/weather")
public class WeatherAdminController {

    private final ExternalDataFetcherService externalDataFetcherService;

    public WeatherAdminController(ExternalDataFetcherService externalDataFetcherService) {
        this.externalDataFetcherService = externalDataFetcherService;
    }

    @PostMapping("/fetch-now")
    public ResponseEntity<String> fetchNow() {
        externalDataFetcherService.fetchAndProcessWeatherData();
        return ResponseEntity.ok("Weather fetch triggered");
    }
}

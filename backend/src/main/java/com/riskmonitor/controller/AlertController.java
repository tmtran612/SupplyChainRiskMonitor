package com.riskmonitor.controller;

import org.springframework.web.bind.annotation.*;
import com.riskmonitor.dto.AlertDTO;
import com.riskmonitor.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.riskmonitor.repository.AlertRepository;
import com.riskmonitor.model.Alert;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping("/active")
    public List<AlertDTO> getActiveAlerts() {
        return alertService.getActiveAlerts();
    }
    
    @PostMapping("/{alertId}/acknowledge")
    public Map<String, String> acknowledgeAlert(@PathVariable UUID alertId) {
        Alert alert = alertRepository.findById(alertId).orElseThrow();
        alert.setAcknowledged(true);
        alertRepository.save(alert);
        Map<String, String> response = new HashMap<>();
        response.put("status", "acknowledged");
        response.put("alertId", String.valueOf(alert.getId()));
        return response;
    }
}
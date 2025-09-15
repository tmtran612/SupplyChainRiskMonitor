package com.riskmonitor.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.riskmonitor.dto.DashboardMetricsDTO;
import com.riskmonitor.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/metrics")
    public DashboardMetricsDTO getDashboardMetrics() {
        return dashboardService.getDashboardMetrics();
    }
}
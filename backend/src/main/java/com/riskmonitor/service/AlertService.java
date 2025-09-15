package com.riskmonitor.service;

import com.riskmonitor.dto.AlertDTO;
import com.riskmonitor.model.Alert;
import com.riskmonitor.repository.AlertRepository;
import com.riskmonitor.repository.RiskScoreRepository;
import com.riskmonitor.model.RiskScore;
import com.riskmonitor.util.RiskLevelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private RiskScoreRepository riskScoreRepository;

    public List<AlertDTO> getActiveAlerts() {
        // Fetch only active & unacknowledged
        List<Alert> raw = alertRepository.findActiveUnacknowledged();
        // Keep only the latest per supplier (ordered already desc). Use LinkedHashMap to preserve order.
        Map<UUID, Alert> latestPerSupplier = new LinkedHashMap<>();
        for (Alert a : raw) {
            UUID supplierId = a.getSupplier().getId();
            if (!latestPerSupplier.containsKey(supplierId)) {
                latestPerSupplier.put(supplierId, a);
            }
        }
        return latestPerSupplier.values().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AlertDTO convertToDTO(Alert alert) {
        RiskScore latestScore = riskScoreRepository.findLatestForSupplier(alert.getSupplier().getId());
        String currentLevel = latestScore != null ? RiskLevelUtil.classifyLabel(latestScore.getRiskScore()) : "unknown";

        return new AlertDTO(
            alert.getId(),
            alert.getSupplier().getName(),
            alert.getTimestamp(),
            alert.getMessage(),
            alert.getSeverity(),
            currentLevel
        );
    }
}

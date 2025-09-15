package com.riskmonitor.service;

import com.riskmonitor.model.Supplier;
import com.riskmonitor.model.RiskScore;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.repository.RiskScoreRepository;
import com.riskmonitor.dto.SupplierRiskDTO;
import com.riskmonitor.dto.SupplierRiskDetailDTO;
import com.riskmonitor.util.RiskLevelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private RiskScoreRepository riskScoreRepository;

    public List<SupplierRiskDTO> getTopRiskSuppliers() {
        List<RiskScore> latestRiskScores = riskScoreRepository.findLatestRiskScores();
        return latestRiskScores.stream()
                .map(latest -> {
                    Supplier supplier = latest.getSupplier();
                    double overallScore = latest.getRiskScore();
                    RiskScore previous = riskScoreRepository.findPreviousForSupplier(supplier.getId());
                    Double previousScore = previous != null ? previous.getRiskScore() : null;
                    Double delta = (previousScore != null) ? (overallScore - previousScore) : null;
                    String reason = latest.getReason();
                    if (reason != null && reason.equalsIgnoreCase("Baseline initialized")) {
                        reason = buildCompactBaselineReason(supplier, overallScore);
                    }
                    return new SupplierRiskDTO(
                            supplier,
                            overallScore,
                            overallScore,
                            overallScore,
                            overallScore,
                            overallScore,
                            RiskLevelUtil.classifyLabel(overallScore),
                            previousScore,
                            delta,
                            reason,
                            latest.getTimestamp()
                    );
                })
                .collect(Collectors.toList());
    }

    public SupplierRiskDetailDTO getSupplierRiskDetail(UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow();
        RiskScore latest = riskScoreRepository.findLatestForSupplier(supplierId);
        double score = latest != null ? latest.getRiskScore() : (supplier.getBaselineRisk() != null ? supplier.getBaselineRisk() : 15.0);
        String level = RiskLevelUtil.classifyLabel(score);
        String reason = latest != null ? latest.getReason() : "Baseline risk";
        if (reason != null && (reason.equalsIgnoreCase("Baseline initialized") || reason.equalsIgnoreCase("Baseline risk"))) {
            reason = buildCompactBaselineReason(supplier, score);
        }
    return new SupplierRiskDetailDTO(
        supplier.getId(),
        supplier.getName(),
        score,
        level,
        reason,
        latest != null ? latest.getTimestamp() : null,
        reason
    );
    }

    private String buildCompactBaselineReason(Supplier s, double baselineScore) {
        // Show only the industry name, nothing else
        String indRaw = s.getIndustry();
        return (indRaw == null || indRaw.isBlank()) ? "Unknown" : cleanIndustry(indRaw);
    }    private String cleanIndustry(String value) {
        String v = value.trim();
        // Keep it as-is but collapse multiple spaces and title-case first letter only (leave existing caps inside words)
        v = v.replaceAll("\\s+", " ");
        if (v.length() == 0) return "Industry?";
        // Avoid over-abbreviation: just ensure first char capitalized
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }

    private String abbreviate(String industry) {
        if (industry == null) return "?";
        String v = industry.toLowerCase();
        if (v.contains("semiconductor equipment")) return "Equip";
        if (v.contains("semiconductor")) return "Semi";
        if (v.contains("electronics")) return "Elec";
        if (v.contains("logistic")) return "Log";
        if (v.contains("raw")) return "Raw";
        if (v.contains("manufactur")) return "Mfg";
        if (v.contains("textile")) return "Text";
        if (v.contains("auto")) return "Auto";
        if (v.contains("pharma")) return "Pharma";
        if (v.contains("mining")) return "Mining";
        if (v.contains("agri")) return "Agri";
        return capitalizeShort(industry, 8);
    }

    private String abbreviateGeo(String loc) {
        if (loc == null || loc.isEmpty()) return "Geo?";
        String l = loc.toLowerCase();
        if (l.contains("south korea") || l.contains("korea")) return "KR";
        if (l.contains("taiwan")) return "TW";
        if (l.contains("united states") || l.contains("usa") || l.contains("u.s.")) return "US";
        if (l.contains("germany")) return "DE";
        if (l.contains("netherlands")) return "NL";
        if (l.contains("china")) return "CN";
        if (l.contains("japan")) return "JP";
        if (l.contains("sweden")) return "SE";
        if (l.contains("brazil")) return "BR";
        if (l.contains("thailand")) return "TH";
        if (l.contains("india")) return "IN";
        if (l.contains("australia")) return "AU";
        if (l.contains(",")) {
            String[] parts = loc.split(",");
            String last = parts[parts.length - 1].trim();
            if (last.length() <= 3) return last.toUpperCase();
        }
        return capitalizeShort(loc, 6);
    }

    private String capitalizeShort(String text, int max) {
        if (text == null) return "?";
        String trimmed = text.trim();
        if (trimmed.length() > max) trimmed = trimmed.substring(0, max);
        if (trimmed.isEmpty()) return "?";
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}

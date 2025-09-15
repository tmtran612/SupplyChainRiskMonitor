package com.riskmonitor.controller;

import com.riskmonitor.model.Supplier;
import com.riskmonitor.model.RiskScore;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.repository.RiskScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/simple-admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SimpleAdminController {

    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @PostMapping("/init-baseline-scores")
    public Map<String, Object> initBaselineScores() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Supplier> suppliers = supplierRepository.findAll();
            int initialized = 0;
            
            for (Supplier supplier : suppliers) {
                // Calculate baseline risk based on industry and tier
                double baselineRisk = calculateBaselineRisk(supplier);
                
                // Set baseline risk on supplier
                supplier.setBaselineRisk(baselineRisk);
                supplierRepository.save(supplier);
                
                // Create initial risk score
                RiskScore riskScore = new RiskScore();
                riskScore.setSupplier(supplier);
                riskScore.setRiskScore(baselineRisk);
                riskScore.setTimestamp(LocalDateTime.now());
                riskScore.setReason(supplier.getIndustry() != null ? supplier.getIndustry() : "Unknown");
                
                riskScoreRepository.save(riskScore);
                initialized++;
            }
            
            result.put("status", "success");
            result.put("message", "Initialized baseline scores for " + initialized + " suppliers");
            result.put("suppliers_processed", initialized);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Failed to initialize baseline scores: " + e.getMessage());
        }
        
        return result;
    }
    
    private double calculateBaselineRisk(Supplier supplier) {
        double baseRisk = 15.0; // Default baseline
        
        // Adjust based on industry
        if (supplier.getIndustry() != null) {
            switch (supplier.getIndustry().toLowerCase()) {
                case "electronics":
                case "raw materials":
                    baseRisk = 25.0; // Higher risk for critical components
                    break;
                case "manufacturing":
                    baseRisk = 20.0;
                    break;
                case "logistics":
                    baseRisk = 18.0;
                    break;
                case "textiles":
                    baseRisk = 12.0; // Lower risk
                    break;
                default:
                    baseRisk = 15.0;
            }
        }
        
        // Adjust based on tier
        if (supplier.getTier() != null) {
            switch (supplier.getTier()) {
                case 1: // Tier 1 - direct suppliers, higher importance
                    baseRisk += 5.0;
                    break;
                case 2: // Tier 2 - moderate importance
                    baseRisk += 2.0;
                    break;
                case 3: // Tier 3 - lower importance
                    baseRisk -= 2.0;
                    break;
            }
        }
        
        return Math.max(5.0, Math.min(50.0, baseRisk)); // Cap between 5-50
    }
}
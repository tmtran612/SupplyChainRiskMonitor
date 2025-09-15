package com.riskmonitor.controller;

import com.riskmonitor.repository.AlertRepository;
import com.riskmonitor.repository.EventRepository;
import com.riskmonitor.repository.RiskScoreRepository;
import com.riskmonitor.service.ExternalDataFetcherService;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.model.Supplier;
import com.riskmonitor.model.RiskScore;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Profile("!prod") // Ensure this controller is not active in a production environment
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdminController {

    @Autowired
    private ExternalDataFetcherService externalDataFetcherService;

    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @PostMapping("/trigger-weather-fetch")
    public ResponseEntity<String> triggerWeatherFetch() {
        try {
            externalDataFetcherService.fetchAndProcessWeatherData();
            return ResponseEntity.ok("Weather data fetch and processing triggered successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to trigger process: " + e.getMessage());
        }
    }

    @PostMapping("/backfill-baseline")
    public ResponseEntity<String> backfillBaseline() {
        int updated = 0;
        int createdScores = 0;
        for (Supplier supplier : supplierRepository.findAll()) {
            boolean changed = false;
            if (supplier.getBaselineRisk() == null || supplier.getBaselineRisk() <= 0) {
                double base = simpleBaseline(supplier);
                supplier.setBaselineRisk(base);
                supplierRepository.save(supplier);
                updated++;
                changed = true;
            }
            if (changed && riskScoreRepository.findLatestForSupplier(supplier.getId()) == null) {
                RiskScore rs = new RiskScore();
                rs.setSupplier(supplier);
                rs.setTimestamp(LocalDateTime.now());
                rs.setRiskScore(supplier.getBaselineRisk());
                rs.setReason(supplier.getIndustry() != null ? supplier.getIndustry() : "Baseline initialized");
                riskScoreRepository.save(rs);
                createdScores++;
            }
        }
        return ResponseEntity.ok("Baseline backfill complete. Suppliers updated=" + updated + ", initial scores created=" + createdScores);
    }

    @PostMapping("/manual-initialize-baseline-scores")
    public ResponseEntity<String> manualInitializeBaselines() {
        int created = 0;
        for (Supplier supplier : supplierRepository.findAll()) {
            if (riskScoreRepository.findLatestForSupplier(supplier.getId()) == null) {
                double base = (supplier.getBaselineRisk() != null && supplier.getBaselineRisk() > 0) ? supplier.getBaselineRisk() : simpleBaseline(supplier);
                if (supplier.getBaselineRisk() == null || supplier.getBaselineRisk() <= 0) {
                    supplier.setBaselineRisk(base);
                    supplierRepository.save(supplier);
                }
                RiskScore rs = new RiskScore();
                rs.setSupplier(supplier);
                rs.setTimestamp(LocalDateTime.now());
                rs.setRiskScore(base);
                rs.setReason(supplier.getIndustry() != null ? supplier.getIndustry() : "Baseline initialized");
                riskScoreRepository.save(rs);
                created++;
            }
        }
        return ResponseEntity.ok("Manual baseline initialization complete. Scores created=" + created);
    }

    @GetMapping("/debug-status")
    public Map<String, Object> debugStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("suppliers", supplierRepository.count());
        map.put("riskScores", riskScoreRepository.count());
        map.put("alerts", alertRepository.count());
        map.put("events", eventRepository.count());
        return map;
    }

    private double simpleBaseline(Supplier supplier) {
        double baseRisk = 15.0;
        if (supplier.getIndustry() != null) {
            switch (supplier.getIndustry().toLowerCase()) {
                case "electronics":
                case "raw materials":
                    baseRisk = 25.0; break;
                case "manufacturing":
                    baseRisk = 20.0; break;
                case "logistics":
                    baseRisk = 18.0; break;
                case "textiles":
                    baseRisk = 12.0; break;
                default:
                    baseRisk = 15.0; break;
            }
        }
        if (supplier.getTier() != null) {
            switch (supplier.getTier()) {
                case 1: baseRisk += 5.0; break;
                case 2: baseRisk += 2.0; break;
                case 3: baseRisk -= 2.0; break;
            }
        }
        return Math.max(5.0, Math.min(50.0, baseRisk));
    }

    @PostMapping("/clear-all-data")
    public String clearAllData() {
        long scoreCount = riskScoreRepository.count();
        riskScoreRepository.deleteAll();
        
        long eventCount = eventRepository.count();
        eventRepository.deleteAll();
        
        long alertCount = alertRepository.count();
        alertRepository.deleteAll();
        
        return "Cleared " + scoreCount + " risk scores, " + eventCount + " events, and " + alertCount + " alerts.";
    }

    @PostMapping("/reset-and-bootstrap")
    public ResponseEntity<String> resetAndBootstrap() {
        try {
            long scoreCount = riskScoreRepository.count();
            long eventCount = eventRepository.count();
            long alertCount = alertRepository.count();
            riskScoreRepository.deleteAll();
            eventRepository.deleteAll();
            alertRepository.deleteAll();

            // Create an initial baseline score per supplier if none exists (ensures dashboard not empty)
            int initialized = 0;
            for (Supplier supplier : supplierRepository.findAll()) {
                if (riskScoreRepository.findLatestForSupplier(supplier.getId()) == null) {
                    double base = (supplier.getBaselineRisk() != null && supplier.getBaselineRisk() > 0) ? supplier.getBaselineRisk() : 15.0; // fallback safety
                    RiskScore rs = new RiskScore();
                    rs.setSupplier(supplier);
                    rs.setTimestamp(LocalDateTime.now());
                    rs.setRiskScore(base);
                    rs.setReason("Baseline initialized");
                    riskScoreRepository.save(rs);
                    initialized++;
                }
            }

            // Trigger weather fetch to layer adjustments
            externalDataFetcherService.fetchAndProcessWeatherData();

            return ResponseEntity.ok("Reset dynamic data (removed " + scoreCount + " scores, " + eventCount + " events, " + alertCount + " alerts) and initialized " + initialized + " baseline scores, then triggered weather fetch.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed reset-and-bootstrap: " + e.getMessage());
        }
    }
    
    @PostMapping("/simple-init")
    public ResponseEntity<String> simpleInit() {
        try {
            List<Supplier> suppliers = supplierRepository.findAll();
            int initialized = 0;
            
            for (Supplier supplier : suppliers) {
                // Calculate baseline risk based on industry and tier
                double baselineRisk = calculateSimpleBaselineRisk(supplier);
                
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
            
            return ResponseEntity.ok("Initialized baseline scores for " + initialized + " suppliers");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed simple init: " + e.getMessage());
        }
    }
    
    private double calculateSimpleBaselineRisk(Supplier supplier) {
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

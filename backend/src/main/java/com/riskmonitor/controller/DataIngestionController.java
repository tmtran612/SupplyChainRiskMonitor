package com.riskmonitor.controller;

import com.riskmonitor.dto.IngestionPayloadDTO;
import com.riskmonitor.service.DataIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DataIngestionController {

    @Autowired
    private DataIngestionService dataIngestionService;

    @PostMapping
    public ResponseEntity<String> ingestData(@RequestBody IngestionPayloadDTO payload) {
        try {
            dataIngestionService.processAndSave(payload);
            return ResponseEntity.ok("Data ingested successfully.");
        } catch (Exception e) {
            // In a real application, you'd have more specific error handling
            return ResponseEntity.status(500).body("Error ingesting data: " + e.getMessage());
        }
    }
}

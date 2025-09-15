package com.riskmonitor.service;

import com.riskmonitor.dto.IngestionPayloadDTO;
import com.riskmonitor.dto.EventPayloadDTO;
import com.riskmonitor.model.Supplier;
import com.riskmonitor.model.Event;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataIngestionService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private EventRepository eventRepository;

    public void processAndSave(IngestionPayloadDTO payload) {
        for (EventPayloadDTO eventPayload : payload.getEvents()) {
            // Find or create supplier
            Supplier supplier = supplierRepository.findByName(eventPayload.getSupplierName())
                .orElseGet(() -> {
                    Supplier newSupplier = new Supplier();
                    newSupplier.setName(eventPayload.getSupplierName());
                    // Set default values for new suppliers
                    newSupplier.setLocation("Unknown");
                    newSupplier.setTier(3); // Default to a lower tier
                    newSupplier.setIndustry("General"); // Default industry
                    newSupplier.setBaselineRisk(0.0);
                    newSupplier.setLastUpdate("");
                    return supplierRepository.save(newSupplier);
                });

            // Create and save event
            Event event = new Event();
            event.setSupplier(supplier);
            event.setType(eventPayload.getEventType());
            event.setPayload(eventPayload.getEventDetails());
            event.setTimestamp(eventPayload.getEventTimestamp());
            eventRepository.save(event);
        }
    }
}

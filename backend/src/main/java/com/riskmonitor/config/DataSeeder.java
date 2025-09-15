package com.riskmonitor.config;

import com.riskmonitor.model.Supplier;
import com.riskmonitor.model.Event;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    public CommandLineRunner seedData(SupplierRepository supplierRepository, EventRepository eventRepository) {
        return args -> {
            if (supplierRepository.count() == 0) {
                // We now rely on the SQL script (005_reseed_suppliers.sql) to be the source of truth.
                // This Java-based seeder is now redundant but kept as a fallback example.
                // If you need to run this, ensure the Supplier constructor matches.
                /*
                Supplier s1 = supplierRepository.save(new Supplier(null, "TechComponents Ltd", "Shenzhen, China", 1, "Electronics", "contact@techcomponents.com", "+86-755-1234-5678", 0.0, null, null, null));
                Supplier s2 = supplierRepository.save(new Supplier(null, "Global Logistics Corp", "Hamburg, Germany", 2, "Logistics", "contact@globallogistics.com", "+49-40-1234-5678", 0.0, null, null, null));
                Supplier s3 = supplierRepository.save(new Supplier(null, "Pacific Materials", "Tokyo, Japan", 1, "Raw Materials", "contact@pacificmaterials.com", "+81-3-1234-5678", 0.0, null, null, null));
                Supplier s4 = supplierRepository.save(new Supplier(null, "American Steel Works", "Pittsburgh, USA", 2, "Manufacturing", "contact@americansteel.com", "+1-412-1234-5678", 0.0, null, null, null));
                Supplier s5 = supplierRepository.save(new Supplier(null, "Nordic Textiles", "Stockholm, Sweden", 3, "Textiles", "contact@nordictextiles.com", "+46-8-1234-5678", 0.0, null, null, null));

                // Minimal seed events (optional illustrative)
                LocalDateTime now = LocalDateTime.now();
                eventRepository.save(new Event(null, s1, now.minusHours(3), "geopolitical_risk", "Export license review in progress"));
                eventRepository.save(new Event(null, s4, now.minusHours(6), "operational_issue", "Furnace downtime reported"));
                */
            }
        };
    }
}

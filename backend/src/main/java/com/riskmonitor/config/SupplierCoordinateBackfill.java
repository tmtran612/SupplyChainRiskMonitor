package com.riskmonitor.config;

import com.riskmonitor.model.Supplier;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.service.ExternalDataFetcherService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SupplierCoordinateBackfill {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Bean
    public CommandLineRunner backfillSupplierCoordinatesAndInitialData(
            SupplierRepository supplierRepository, 
            ExternalDataFetcherService externalDataFetcherService) {
        return args -> {
            for (Supplier supplier : supplierRepository.findAll()) {
                boolean needsGeocoding = (supplier.getLatitude() == null || supplier.getLongitude() == null);
                
                if (needsGeocoding) {
                    String location = supplier.getLocation();
                    String encoded = URLEncoder.encode(location, StandardCharsets.UTF_8);
                    try {
                        String geoUrl = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s", encoded, apiKey);
                        String response = restTemplate.getForObject(geoUrl, String.class);
                        if (response == null || response.equals("[]")) {
                            System.err.println("Backfill: No geocoding result for location: " + location);
                            continue;
                        }
                        JSONArray arr = new JSONArray(response);
                        JSONObject obj = arr.getJSONObject(0);
                        supplier.setLatitude(obj.getDouble("lat"));
                        supplier.setLongitude(obj.getDouble("lon"));
                        supplierRepository.save(supplier);
                        System.out.println("Backfill: Saved coordinates for " + supplier.getName() + " (" + location + ")");
                    } catch (Exception ex) {
                        System.err.println("Backfill: Failed to geocode " + location + ": " + ex.getMessage());
                        continue;
                    }
                }
                
                // Immediately fetch initial weather data and create risk scores for this supplier
                try {
                    externalDataFetcherService.fetchAndProcessWeatherDataForSupplier(supplier);
                    System.out.println("Initial data fetch completed for " + supplier.getName());
                } catch (Exception ex) {
                    System.err.println("Initial data fetch failed for " + supplier.getName() + ": " + ex.getMessage());
                }
            }
        };
    }
}

package com.riskmonitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riskmonitor.dto.weather.WeatherResponseDTO;
import com.riskmonitor.model.Event;
import com.riskmonitor.model.Supplier;
import com.riskmonitor.repository.EventRepository;
import com.riskmonitor.repository.AlertRepository;
import com.riskmonitor.util.RiskLevelUtil;
import com.riskmonitor.model.Alert;
import com.riskmonitor.repository.RiskScoreRepository;
import com.riskmonitor.model.RiskScore;
import com.riskmonitor.repository.SupplierRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExternalDataFetcherService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExternalDataFetcherService.class);

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile boolean initialFetchDone = false;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!initialFetchDone) {
            initialFetchDone = true;
            logger.info("Application started. Fetching initial weather data for all suppliers (one-time)...");
            fetchAndProcessWeatherData();
        }
    }

    @Scheduled(fixedRate = 3600000) // Fetch weather data every hour
    public void fetchAndProcessWeatherData() {
        for (Supplier supplier : supplierRepository.findAll()) {
            fetchAndProcessWeatherDataForSupplier(supplier);
        }
    }

    public void fetchAndProcessWeatherDataForSupplier(Supplier supplier) {
        // Attempt to resolve coordinates (cached / static / API). If cannot, log once per interval and stop.
        double[] coords = resolveCoordinates(supplier);
        if (coords == null) {
            return; // resolution already logged inside resolveCoordinates
        }
        double lat = coords[0];
        double lon = coords[1];
        try {

            String weatherUrl = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric", apiUrl, lat, lon, apiKey);
            String rawResponse = restTemplate.getForObject(weatherUrl, String.class);
            logger.info("Raw weather response for {}: {}", supplier.getName(), rawResponse);

            WeatherResponseDTO weatherData;
            try {
                weatherData = objectMapper.readValue(rawResponse, WeatherResponseDTO.class);
            } catch (Exception parseEx) {
                logger.error("Weather JSON parse failed for {}: {}", supplier.getName(), parseEx.getMessage());
                return; // skip this cycle for this supplier; baseline may still be set next run
            }

            if (weatherData != null && weatherData.getWeather() != null && weatherData.getWeather().length > 0) {
                String weatherDescription = weatherData.getWeather()[0].getDescription();
                double temperature = weatherData.getMain().getTemp();
                Double windSpeed = weatherData.getWind() != null ? weatherData.getWind().getSpeed() : null;
                
                Double rainMm = (weatherData.getRain() != null && weatherData.getRain().getOneHour() != null) ? weatherData.getRain().getOneHour() : null;
                Double snowMm = (weatherData.getSnow() != null && weatherData.getSnow().getOneHour() != null) ? weatherData.getSnow().getOneHour() : null;
                Double precipMm = rainMm != null ? rainMm : snowMm;

                Integer conditionId = weatherData.getWeather()[0].getId();

                String payload = String.format("lat=%.4f,lon=%.4f; condition=%s(id=%d); temp=%.1f°C%s%s",
                    lat, lon, weatherDescription, conditionId, temperature,
                    windSpeed != null ? "; wind=" + windSpeed + " m/s" : "",
                    precipMm != null ? "; precip=" + String.format("%.1fmm", precipMm) : "");

                Event weatherEvent = new Event();
                weatherEvent.setSupplier(supplier);
                weatherEvent.setTimestamp(LocalDateTime.now());
                weatherEvent.setType("weather_update");
                weatherEvent.setPayload(payload);
                weatherEvent = eventRepository.save(weatherEvent);

                // --- Risk adjustment logic (amplified) ---
                boolean heavyMode = true; // temporary amplification to surface CRITICAL examples
                double adjustment = 0.0;
                List<String> reasonParts = new ArrayList<>();
                List<String> factorTokens = new ArrayList<>();

                // 1. Precipitation
                double precipAdj = 0.0;
                if (precipMm != null) {
                    if (precipMm > 20) precipAdj = heavyMode ? 18 : 8; // Torrential
                    else if (precipMm > 7.6) precipAdj = heavyMode ? 14 : 6; // Heavy
                    else if (precipMm > 2.5) precipAdj = heavyMode ? 9 : 3; // Moderate
                    else precipAdj = heavyMode ? 4 : 1; // Light
                    if (precipAdj > 0) {
                        reasonParts.add(String.format("precip:%.1fmm->+%.1f", precipMm, precipAdj));
                        factorTokens.add("severe_precipitation");
                    }
                }

                // 2. Temperature
                double tempAdj = 0.0;
                if (temperature < -15) tempAdj = heavyMode ? 12 : 5; // Severe cold
                else if (temperature < -5) tempAdj = heavyMode ? 7 : 3; // Cold
                else if (temperature > 45) tempAdj = heavyMode ? 15 : 6; // Severe heat
                else if (temperature > 38) tempAdj = heavyMode ? 10 : 4; // Heat
                if (tempAdj > 0) {
                    reasonParts.add(String.format("temp:%.1fC->+%.1f", temperature, tempAdj));
                    factorTokens.add("extreme_temperature");
                }

                // 3. Wind
                double windAdj = 0.0;
                if (windSpeed != null) {
                    if (windSpeed > 25) windAdj = heavyMode ? 14 : 7; // Violent storm
                    else if (windSpeed > 15) windAdj = heavyMode ? 9 : 4; // Strong gale
                    else if (windSpeed > 10) windAdj = heavyMode ? 5 : 2; // Fresh breeze
                    if (windAdj > 0) {
                        reasonParts.add(String.format("wind:%.1fm/s->+%.1f", windSpeed, windAdj));
                        factorTokens.add("high_wind");
                    }
                }

                adjustment = precipAdj + tempAdj + windAdj;
                double cap = heavyMode ? 35.0 : 10.0;
                if (adjustment > cap) adjustment = cap; // amplified cap while heavyMode true

                // Initialize baseline risk if appears uninitialized (0 or extremely low) and no prior scores
                if ((supplier.getBaselineRisk() == null || supplier.getBaselineRisk() <= 0.0) && riskScoreRepository.findLatestForSupplier(supplier.getId()) == null) {
                    double baselineRisk = calculateBaselineRisk(supplier);
                    supplier.setBaselineRisk(baselineRisk);
                    supplierRepository.save(supplier);
                }

                double baselineValue = supplier.getBaselineRisk() != null ? supplier.getBaselineRisk() : 15.0;
                double proposedScore = Math.min(100.0, baselineValue + adjustment);
                if (heavyMode && proposedScore < 55 && adjustment > 0) {
                    // Ensure at least a HIGH classification for visibility when weather hits
                    proposedScore = Math.max(proposedScore, baselineValue + Math.min(25, adjustment + 10));
                }

                final double finalProposedScore = proposedScore;

                RiskScore latest = riskScoreRepository.findLatestForSupplier(supplier.getId());
                boolean shouldCreate = false;
                double previousScore = latest != null ? latest.getRiskScore() : -1;
                LocalDateTime now = LocalDateTime.now();

                if (latest == null) {
                    shouldCreate = true;
                } else {
                    double delta = Math.abs(finalProposedScore - latest.getRiskScore());
                    boolean levelChanged = RiskLevelUtil.classify(finalProposedScore) != RiskLevelUtil.classify(latest.getRiskScore());
                    boolean ageExceeded = latest.getTimestamp().isBefore(now.minusHours(1));
                    if (delta >= 5 || levelChanged || ageExceeded) {
                        shouldCreate = true;
                    }
                }

                if (shouldCreate) {
                    RiskScore score = new RiskScore();
                    score.setSupplier(supplier);
                    score.setTimestamp(now);
                    score.setRiskScore(finalProposedScore);
                    if (adjustment > 0) {
                        String reasonStr = "Weather adjustment: +" + String.format("%.1f", adjustment) + " (" + String.join("; ", reasonParts) + ")";
                        score.setReason(reasonStr);
                        
                        Event riskEvent = new Event();
                        riskEvent.setSupplier(supplier);
                        riskEvent.setTimestamp(now);
                        riskEvent.setType("weather_risk_adjustment");
                        riskEvent.setPayload(String.format("adjustment=%.1f; reasons=%s", adjustment, String.join(",", factorTokens)));
                        eventRepository.save(riskEvent);
                    } else {
                        score.setReason(String.format("Weather stable: %s temp=%.1fC%s", weatherDescription, temperature,
                                windSpeed != null ? "; wind=" + String.format("%.1f", windSpeed) + "m/s" : ""));
                    }
                    riskScoreRepository.save(score);

                    if (latest == null || RiskLevelUtil.classify(finalProposedScore).ordinal() > RiskLevelUtil.classify(previousScore).ordinal() || Math.abs(finalProposedScore - previousScore) >= 15) {
                        boolean recentSameSeverity = alertRepository.findBySupplierIdOrderByTimestampDesc(supplier.getId()).stream()
                                .limit(5)
                                .anyMatch(a -> a.getSeverity().equalsIgnoreCase(RiskLevelUtil.classifyLabel(finalProposedScore)) && a.getTimestamp().isAfter(now.minusHours(2)));
                        if (!recentSameSeverity) {
                            Alert alert = new Alert();
                            alert.setSupplier(supplier);
                            alert.setTimestamp(now);
                            alert.setSeverity(RiskLevelUtil.classifyLabel(finalProposedScore));
                            alert.setMessage(String.format("Risk score changed to %.1f (%s)%s", finalProposedScore, RiskLevelUtil.classifyLabel(finalProposedScore), adjustment > 0 ? " due to weather: " + String.join(", ", factorTokens) : ""));
                            alertRepository.save(alert);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch weather data for {}: {}", supplier.getName(), e.getMessage(), e);
        }
    }

    /**
     * Calculate baseline risk based on supplier characteristics following industry standards
     */
    private double calculateBaselineRisk(Supplier supplier) {
        double baselineRisk = 0.0;
        
        // Industry risk factor
        switch (supplier.getIndustry().toLowerCase()) {
            case "semiconductors":
            case "electronics":
                baselineRisk += 25.0; // High complexity, supply chain dependencies
                break;
            case "semiconductor equipment":
                baselineRisk += 20.0; // Specialized equipment, fewer suppliers
                break;
            case "automotive":
                baselineRisk += 15.0; // Mature industry, established supply chains
                break;
            case "textiles":
            case "agriculture":
                baselineRisk += 10.0; // Lower complexity
                break;
            default:
                baselineRisk += 12.0; // Default industry risk
        }
        
        // Tier-based risk (higher tier = more critical = higher baseline risk)
        if (supplier.getTier() != null) {
            switch (supplier.getTier()) {
                case 1:
                    baselineRisk += 15.0; // Critical tier-1 suppliers
                    break;
                case 2:
                    baselineRisk += 8.0; // Important tier-2 suppliers
                    break;
                case 3:
                    baselineRisk += 3.0; // Lower-tier suppliers
                    break;
            }
        }
        
        // Geographic risk factor
        String location = supplier.getLocation() != null ? supplier.getLocation().toLowerCase() : "";
        if (location.contains("china") || location.contains("taiwan")) {
            baselineRisk += 12.0; // Geopolitical considerations
        } else if (location.contains("usa") || location.contains("germany") || location.contains("netherlands")) {
            baselineRisk += 5.0; // Stable regions
        } else if (location.contains("korea") || location.contains("japan")) {
            baselineRisk += 8.0; // Moderate risk regions
        } else {
            baselineRisk += 10.0; // Default geographic risk
        }
        
        return Math.min(50.0, baselineRisk); // Cap baseline at 50% to allow weather adjustments
    }

    // -------------------------------- Geocoding Enhancement Section --------------------------------
    private static final Map<String, double[]> STATIC_COORDS = createStaticCoords();
    private static final Map<String, String> COUNTRY_CODE = createCountryCodes();
    private static final Map<String, LocalDateTime> GEO_FAILURE_CACHE = new ConcurrentHashMap<>();
    private static final long GEO_RETRY_MINUTES = 30; // skip repeated failures within this window

    private static Map<String, double[]> createStaticCoords() {
        Map<String, double[]> m = new HashMap<>();
        m.put("shenzhen, china", new double[]{22.5431, 114.0579});
        m.put("hamburg, germany", new double[]{53.5511, 9.9937});
        m.put("tokyo, japan", new double[]{35.6762, 139.6503});
        m.put("pittsburgh, usa", new double[]{40.4406, -79.9959});
        m.put("stockholm, sweden", new double[]{59.3293, 18.0686});
        return m;
    }

    private static Map<String, String> createCountryCodes() {
        Map<String, String> m = new HashMap<>();
        m.put("china", "CN");
        m.put("germany", "DE");
        m.put("japan", "JP");
        m.put("usa", "US");
        m.put("united states", "US");
        m.put("sweden", "SE");
        m.put("south korea", "KR");
        m.put("korea", "KR");
        m.put("netherlands", "NL");
        m.put("taiwan", "TW");
        return m;
    }

    private double[] resolveCoordinates(Supplier supplier) {
        if (supplier.getLatitude() != null && supplier.getLongitude() != null) {
            return new double[]{supplier.getLatitude(), supplier.getLongitude()};
        }
        String raw = supplier.getLocation();
        if (raw == null || raw.isBlank()) {
            logger.warn("Supplier {} has no location string.", supplier.getName());
            return null;
        }
        String key = raw.toLowerCase().trim();

        // Check cached failure window
        LocalDateTime lastFail = GEO_FAILURE_CACHE.get(key);
        if (lastFail != null && lastFail.isAfter(LocalDateTime.now().minusMinutes(GEO_RETRY_MINUTES))) {
            return null; // skip repeated failing lookups for a while
        }

        // Static known coords
        if (STATIC_COORDS.containsKey(key)) {
            double[] coords = STATIC_COORDS.get(key);
            persistCoords(supplier, coords);
            logger.info("Using static fallback coordinates for {} -> {},{}", raw, coords[0], coords[1]);
            return coords;
        }

        // Try parsing city,country
        String city = raw;
        String country = null;
        if (raw.contains(",")) {
            String[] parts = raw.split(",");
            city = parts[0].trim();
            country = parts[parts.length - 1].trim();
        }

        List<String> queries = new ArrayList<>();
        // 1. full raw
        queries.add(raw);
        // 2. city + ISO country code
        if (country != null) {
            String code = COUNTRY_CODE.get(country.toLowerCase());
            if (code != null) queries.add(city + "," + code);
            queries.add(city + "," + country); // e.g. Pittsburgh, USA
        }
        // 3. just city
        queries.add(city);

        for (String q : queries) {
            try {
                String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
                String geoUrl = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s", encoded, apiKey);
                String geoResponse = restTemplate.getForObject(geoUrl, String.class);
                if (geoResponse != null && !geoResponse.equals("[]")) {
                    JSONArray geoArray = new JSONArray(geoResponse);
                    JSONObject geoObj = geoArray.getJSONObject(0);
                    double lat = geoObj.getDouble("lat");
                    double lon = geoObj.getDouble("lon");
                    double[] coords = new double[]{lat, lon};
                    persistCoords(supplier, coords);
                    logger.info("Geocoded '{}' via query '{}' -> {},{}", raw, q, lat, lon);
                    return coords;
                }
            } catch (Exception ex) {
                logger.debug("Geocode attempt failed for '{}' query '{}': {}", raw, q, ex.getMessage());
            }
        }

        // Static fallback if we can map by city alone
        String cityLower = city.toLowerCase();
        for (Map.Entry<String,double[]> e : STATIC_COORDS.entrySet()) {
            if (e.getKey().startsWith(cityLower + ",")) {
                double[] coords = e.getValue();
                persistCoords(supplier, coords);
                logger.info("Using partial static fallback for {} -> {},{}", raw, coords[0], coords[1]);
                return coords;
            }
        }

        logger.error("No geocoding result for location: {} (after multiple strategies)", raw);
        GEO_FAILURE_CACHE.put(key, LocalDateTime.now());
        return null;
    }

    private void persistCoords(Supplier supplier, double[] coords) {
        supplier.setLatitude(coords[0]);
        supplier.setLongitude(coords[1]);
        try {
            supplierRepository.save(supplier);
        } catch (Exception e) {
            logger.warn("Failed persisting coordinates for {}: {}", supplier.getName(), e.getMessage());
        }
    }
}
package com.duong.salesmanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeocodingService {
    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Convert address string to Coordinates (Lat/Lng) using Nominatim OpenStreetMap API.
     * @param address The full address string
     * @return Map containing "lat" and "lng", or null if not found
     */
    public Map<String, Double> getCoordinates(String address) {
        if (address == null || address.isBlank()) return null;

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .toUriString();

            // Nominatim requires a User-Agent to avoid being blocked
            logger.info("Geocoding address: {}", address);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "FoodDeliveryApp/1.0");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    url, 
                    org.springframework.http.HttpMethod.GET, 
                    entity, 
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && !root.isEmpty()) {
                JsonNode firstResult = root.get(0);
                Map<String, Double> coords = new HashMap<>();
                coords.put("lat", firstResult.get("lat").asDouble());
                coords.put("lng", firstResult.get("lon").asDouble());
                return coords;
            }
        } catch (Exception e) {
            logger.error("Error calling Nominatim API for address: {}", address, e);
        }
        return null;
    }
}

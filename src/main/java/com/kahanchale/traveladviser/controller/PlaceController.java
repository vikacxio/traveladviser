package com.kahanchale.traveladviser.controller;

import com.kahanchale.traveladviser.dto.PlaceDTO;
import com.kahanchale.traveladviser.response.SearchPlacesRequest;
import com.kahanchale.traveladviser.service.PlaceService;
import com.kahanchale.traveladviser.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Get nearby places based on current location
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusKm Search radius in kilometers (default 5)
     * @param limit Number of results (default 10)
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceDTO>> getNearbyPlaces(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Integer radiusKm,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<PlaceDTO> places = placeService.getNearbyPlaces(latitude, longitude, radiusKm, limit);
        return ResponseEntity.ok(places);
    }

    /**
     * Search places with filters
     */
    @PostMapping("/search")
    public ResponseEntity<List<PlaceDTO>> searchPlaces(@RequestBody SearchPlacesRequest request) {
        List<PlaceDTO> places = placeService.searchPlaces(request);
        return ResponseEntity.ok(places);
    }

    /**
     * Get weather-based recommendations
     * Suggests places based on current weather at location
     */
    @GetMapping("/recommendations/weather")
    public ResponseEntity<List<PlaceDTO>> getWeatherRecommendations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<PlaceDTO> places = recommendationService.getWeatherBasedRecommendations(latitude, longitude, limit);
        return ResponseEntity.ok(places);
    }

    /**
     * Get trending places
     */
    @GetMapping("/trending")
    public ResponseEntity<List<PlaceDTO>> getTrendingPlaces(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<PlaceDTO> places = recommendationService.getTrendingPlaces(limit);
        return ResponseEntity.ok(places);
    }

    /**
     * Get places by city
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<PlaceDTO>> getPlacesByCity(@PathVariable String city) {
        List<PlaceDTO> places = placeService.getPlacesByCity(city);
        return ResponseEntity.ok(places);
    }

    /**
     * Get single place by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlaceDTO> getPlaceById(@PathVariable Long id) {
        PlaceDTO place = placeService.getPlaceById(id);
        return ResponseEntity.ok(place);
    }
}

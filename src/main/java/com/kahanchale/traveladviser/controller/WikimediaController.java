package com.kahanchale.traveladviser.controller;

import com.kahanchale.traveladviser.dto.PlaceImageDTO;
import com.kahanchale.traveladviser.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wikimedia")
@CrossOrigin(origins = "*")
@Slf4j
public class WikimediaController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/places/{placeId}/fetch")
    public ResponseEntity<?> fetchWikimediaImagesForPlace(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "3") int count) {
        List<PlaceImageDTO> images = imageService.fetchAndStoreWikimediaImages(placeId, count);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Wikimedia images fetched and stored successfully");
        response.put("placeId", placeId);
        response.put("count", images.size());
        response.put("images", images);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/places/fetch-all")
    public ResponseEntity<?> fetchWikimediaImagesForAllPlacesWithDescription(
            @RequestParam(defaultValue = "1") int countPerPlace) {
        List<PlaceImageDTO> images = imageService.fetchAndStoreWikimediaImagesForPlacesWithDescription(countPerPlace);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Wikimedia images fetched and stored for places with descriptions");
        response.put("count", images.size());
        response.put("images", images);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}

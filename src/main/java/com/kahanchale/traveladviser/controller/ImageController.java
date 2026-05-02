package com.kahanchale.traveladviser.controller;

import com.kahanchale.traveladviser.dto.ImageUploadRequest;
import com.kahanchale.traveladviser.dto.PlaceImageDTO;
import com.kahanchale.traveladviser.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/places/{placeId}/images")
@CrossOrigin(origins = "*")
@Slf4j
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * Upload image for a place
     * Accept Base64 encoded image
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long placeId,
            @RequestBody ImageUploadRequest request) {
        try {
            PlaceImageDTO image = imageService.uploadImage(placeId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("image", image);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading image for place {}: {}", placeId, e.getMessage());
            return buildErrorResponse("Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Fetch images from Unsplash and store in database
     * Uses place name as search query
     */
    @PostMapping("/unsplash/fetch")
    public ResponseEntity<?> fetchUnsplashImages(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "3") int count) {
        try {
            List<PlaceImageDTO> images = imageService.fetchAndStoreUnsplashImages(placeId, count);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Images fetched from Unsplash successfully");
            response.put("count", images.size());
            response.put("images", images);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching Unsplash images for place {}: {}", placeId, e.getMessage());
            return buildErrorResponse("Failed to fetch Unsplash images: " + e.getMessage());
        }
    }

    /**
     * Get all images for a place
     */
    @GetMapping
    public ResponseEntity<?> getImages(@PathVariable Long placeId) {
        try {
            List<PlaceImageDTO> images = imageService.getImagesForPlace(placeId);
            Map<String, Object> response = new HashMap<>();
            response.put("placeId", placeId);
            response.put("count", images.size());
            response.put("images", images);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching images for place {}: {}", placeId, e.getMessage());
            return buildErrorResponse("Failed to fetch images: " + e.getMessage());
        }
    }

    /**
     * Get primary image for a place
     */
    @GetMapping("/primary")
    public ResponseEntity<?> getPrimaryImage(@PathVariable Long placeId) {
        try {
            PlaceImageDTO image = imageService.getPrimaryImage(placeId);
            if (image == null) {
                return buildErrorResponse("No primary image found for this place");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("placeId", placeId);
            response.put("image", image);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching primary image for place {}: {}", placeId, e.getMessage());
            return buildErrorResponse("Failed to fetch primary image: " + e.getMessage());
        }
    }

    /**
     * Get image by ID (binary data)
     */
    @GetMapping("/{imageId}/download")
    public ResponseEntity<?> downloadImage(
            @PathVariable Long placeId,
            @PathVariable Long imageId) {
        try {
            byte[] imageData = imageService.getImageData(imageId);
            if (imageData == null || imageData.length == 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageData);
        } catch (Exception e) {
            log.error("Error downloading image {}: {}", imageId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Set image as primary
     */
    @PutMapping("/{imageId}/set-primary")
    public ResponseEntity<?> setPrimaryImage(
            @PathVariable Long placeId,
            @PathVariable Long imageId) {
        try {
            imageService.setPrimaryImage(placeId, imageId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image set as primary successfully");
            response.put("placeId", placeId);
            response.put("imageId", imageId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error setting primary image for place {}: {}", placeId, e.getMessage());
            return buildErrorResponse("Failed to set primary image: " + e.getMessage());
        }
    }

    /**
     * Delete image
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long placeId,
            @PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image deleted successfully");
            response.put("imageId", imageId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting image {}: {}", imageId, e.getMessage());
            return buildErrorResponse("Failed to delete image: " + e.getMessage());
        }
    }

    /**
     * Get image count for a place
     */
    @GetMapping("/count")
    public ResponseEntity<?> getImageCount(@PathVariable Long placeId) {
        try {
            int count = imageService.getImageCount(placeId);
            Map<String, Object> response = new HashMap<>();
            response.put("placeId", placeId);
            response.put("imageCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting image count for place {}: {}", placeId, e.getMessage());
            return buildErrorResponse("Failed to get image count: " + e.getMessage());
        }
    }

    /**
     * Helper method to build error response
     */
    private ResponseEntity<?> buildErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return ResponseEntity.badRequest().body(error);
    }
}

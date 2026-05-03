package com.kahanchale.traveladviser.service;

import com.kahanchale.traveladviser.request.ImageUploadRequest;
import com.kahanchale.traveladviser.dto.PlaceImageDTO;
import com.kahanchale.traveladviser.dto.UnsplashPhotoDTO;
import com.kahanchale.traveladviser.entity.Place;
import com.kahanchale.traveladviser.entity.PlaceImage;
import com.kahanchale.traveladviser.repository.PlaceImageRepository;
import com.kahanchale.traveladviser.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ImageService {

    @Autowired
    private PlaceImageRepository placeImageRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UnsplashService unsplashService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Upload image from Base64 data
     */
    public PlaceImageDTO uploadImage(Long placeId, ImageUploadRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found with ID: " + placeId));

        try {
            // Decode Base64 image
            String imageData = request.getImageData();
            if (imageData.contains(",")) {
                imageData = imageData.substring(imageData.indexOf(",") + 1);
            }
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            PlaceImage placeImage = new PlaceImage();
            placeImage.setPlace(place);
            placeImage.setImageData(imageBytes);
            placeImage.setImageSize((long) imageBytes.length);
            placeImage.setContentType(getContentTypeFromBase64(request.getImageData()));
            placeImage.setImageName(request.getImageName() != null ? request.getImageName() : "uploaded_" + System.currentTimeMillis());
            placeImage.setSource("UPLOAD");
            boolean shouldBePrimary = request.isPrimary() || placeImageRepository.findPrimaryImageByPlaceId(placeId).isEmpty();
            placeImage.setPrimary(shouldBePrimary);
            if (shouldBePrimary) {
                unsetAllPrimaryForPlace(placeId);
            }

            PlaceImage saved = placeImageRepository.save(placeImage);
            log.info("Image uploaded successfully for place ID: {}", placeId);

            return convertToDTO(saved);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 image data");
        }
    }

    /**
     * Fetch images from Unsplash and store in database
     */
    public List<PlaceImageDTO> fetchAndStoreUnsplashImages(Long placeId, int count) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found with ID: " + placeId));

        if (!unsplashService.isConfigured()) {
            log.warn("Unsplash API not configured");
            return List.of();
        }

        List<UnsplashPhotoDTO> unsplashPhotos = unsplashService.fetchImagesForPlace(place.getName(), count);
        List<PlaceImageDTO> storedImages = unsplashPhotos.stream()
                .map(photo -> storeUnsplashImage(place, photo))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Stored {} images from Unsplash for place: {}", storedImages.size(), place.getName());
        return storedImages;
    }

    /**
     * Store single Unsplash image
     */
    private PlaceImageDTO storeUnsplashImage(Place place, UnsplashPhotoDTO photo) {
        try {
            if (photo == null || photo.getUrls() == null || photo.getUrls().getRegular() == null) {
                log.warn("Skipping Unsplash photo with missing URL for place: {}", place.getName());
                return null;
            }

            String imageUrl = photo.getUrls().getRegular(); // Use regular size for database storage
            if (placeImageRepository.existsByPlaceIdAndSourceUrl(place.getId(), imageUrl)) {
                log.info("Skipping duplicate Unsplash image URL for place {}: {}", place.getId(), imageUrl);
                return null;
            }
            byte[] imageBytes = unsplashService.downloadImage(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Skipping Unsplash photo with empty download for place: {}", place.getName());
                return null;
            }

            PlaceImage placeImage = new PlaceImage();
            placeImage.setPlace(place);
            placeImage.setImageData(imageBytes);
            placeImage.setImageSize((long) imageBytes.length);
            placeImage.setContentType("image/jpeg");
            placeImage.setImageName(buildUnsplashImageName(photo));
            placeImage.setSource("UNSPLASH");
            placeImage.setSourceUrl(imageUrl);
            boolean shouldBePrimary = placeImageRepository.findPrimaryImageByPlaceId(place.getId()).isEmpty();
            placeImage.setPrimary(shouldBePrimary);
            if (shouldBePrimary) {
                unsetAllPrimaryForPlace(place.getId());
            }

            PlaceImage saved = placeImageRepository.save(placeImage);
            log.info("Stored Unsplash image for place: {}", place.getName());

            return convertToDTO(saved);
        } catch (Exception e) {
            log.error("Error storing Unsplash image for place: {}", place.getName(), e);
            return null;
        }
    }

    private String buildUnsplashImageName(UnsplashPhotoDTO photo) {
        String baseName = photo.getId() != null ? "unsplash_" + photo.getId() : "unsplash_" + System.currentTimeMillis();
        String altDescription = photo.getAlt_description();

        if (altDescription == null || altDescription.isBlank()) {
            return baseName + ".jpg";
        }

        String sanitized = altDescription.replaceAll("[^a-zA-Z0-9-_ ]", "").trim().replace(' ', '_');
        if (sanitized.isBlank()) {
            return baseName + ".jpg";
        }

        String combined = baseName + "_" + sanitized + ".jpg";
        return combined.length() > 500 ? combined.substring(0, 500) : combined;
    }

    /**
     * Get all images for a place
     */
    public List<PlaceImageDTO> getImagesForPlace(Long placeId) {
        boolean placeExists = placeRepository.existsById(placeId);
        if (!placeExists) {
            throw new RuntimeException("Place not found with ID: " + placeId);
        }

        return placeImageRepository.findByPlaceId(placeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get primary image for a place
     */
    public PlaceImageDTO getPrimaryImage(Long placeId) {
        return placeImageRepository.findPrimaryImageByPlaceId(placeId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Get image by ID
     */
    public byte[] getImageData(Long imageId) {
        PlaceImage image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));
        return image.getImageData();
    }

    /**
     * Set image as primary
     */
    public void setPrimaryImage(Long placeId, Long imageId) {
        PlaceImage image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));

        if (!image.getPlace().getId().equals(placeId)) {
            throw new RuntimeException("Image does not belong to this place");
        }

        // Unset all other primary images
        unsetAllPrimaryForPlace(placeId);

        // Set this image as primary
        image.setPrimary(true);
        placeImageRepository.save(image);
        log.info("Set image {} as primary for place {}", imageId, placeId);
    }

    /**
     * Delete image
     */
    public void deleteImage(Long imageId) {
        PlaceImage image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));
        Long placeId = image.getPlace().getId();
        boolean deletingPrimary = image.isPrimary();
        placeImageRepository.delete(image);

        // Keep invariant: if images exist for the place, exactly one must be primary.
        if (deletingPrimary) {
            placeImageRepository.findFirstByPlaceIdOrderByCreatedAtAsc(placeId).ifPresent(nextPrimary -> {
                unsetAllPrimaryForPlace(placeId);
                nextPrimary.setPrimary(true);
                placeImageRepository.save(nextPrimary);
            });
        }

        log.info("Deleted image with ID: {}", imageId);
    }

    private void unsetAllPrimaryForPlace(Long placeId) {
        placeImageRepository.findByPlaceId(placeId).forEach(img -> {
            if (img.isPrimary()) {
                img.setPrimary(false);
                placeImageRepository.save(img);
            }
        });
    }

    /**
     * Convert PlaceImage to DTO
     */
    private PlaceImageDTO convertToDTO(PlaceImage image) {
        PlaceImageDTO dto = new PlaceImageDTO();
        dto.setId(image.getId());
        dto.setPlaceId(image.getPlace().getId());
        dto.setContentType(image.getContentType());
        dto.setPrimary(image.isPrimary());
        dto.setImageName(image.getImageName());
        dto.setImageSize(image.getImageSize());
        dto.setSource(image.getSource());
        dto.setSourceUrl(image.getSourceUrl());
        dto.setCreatedAt(image.getCreatedAt().format(DATE_FORMATTER));

        // Keep DTO URL-based only (future S3/CDN); binary stays in DB
        if (image.getImageUrl() != null) {
            dto.setImageUrl(image.getImageUrl()); // S3 URL
        }

        return dto;
    }

    /**
     * Extract content type from Base64 data URI
     */
    private String getContentTypeFromBase64(String base64Data) {
        if (base64Data.contains("data:")) {
            int start = base64Data.indexOf("data:") + 5;
            int end = base64Data.indexOf(";");
            if (end > start) {
                return base64Data.substring(start, end);
            }
        }
        return "image/jpeg"; // Default
    }

    /**
     * Get image count for a place
     */
    public int getImageCount(Long placeId) {
        Integer count = placeImageRepository.countByPlaceId(placeId);
        return count != null ? count : 0;
    }
}

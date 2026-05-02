package com.kahanchale.traveladviser.service;

import com.kahanchale.traveladviser.dto.UnsplashPhotoDTO;
import com.kahanchale.traveladviser.dto.UnsplashSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UnsplashService {

    @Value("${unsplash.api.key:}")
    private String unsplashApiKey;

    @Value("${unsplash.api.url:https://api.unsplash.com}")
    private String unsplashApiUrl;

    private final RestTemplate restTemplate;

    public UnsplashService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch images for a place from Unsplash API
     */
    public List<UnsplashPhotoDTO> fetchImagesForPlace(String query, int count) {
        if (!isConfigured()) {
            log.warn("Unsplash API key not configured");
            return Collections.emptyList();
        }

        try {
            String url = UriComponentsBuilder
                    .fromUriString(unsplashApiUrl + "/search/photos")
                    .queryParam("query", query)
                    .queryParam("per_page", Math.min(Math.max(count, 1), 30))
                    .queryParam("client_id", unsplashApiKey)
                    .build()
                    .toUriString();

            UnsplashSearchResponse response =
                    restTemplate.getForObject(url, UnsplashSearchResponse.class);

            if (response != null && response.getResults() != null) {
                log.info("Fetched {} images from Unsplash for query: {}",
                        response.getResults().length, query);

                return Arrays.asList(response.getResults());
            }

        } catch (Exception e) {
            log.error("Error fetching images from Unsplash for query: {}", query, e);
        }

        return Collections.emptyList();
    }

    /**
     * Download image bytes using RestTemplate (modern approach)
     */
    public byte[] downloadImage(String imageUrl) {
        try {
            return restTemplate.getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            log.error("Error downloading image from URL: {}", imageUrl, e);
            return new byte[0];
        }
    }

    /**
     * Get content type from image URL
     */
    public String getContentType(String imageUrl) {
        try {
            return restTemplate.headForHeaders(imageUrl).getContentType().toString();
        } catch (Exception e) {
            log.warn("Could not determine content type for: {}", imageUrl);
            return "image/jpeg";
        }
    }

    /**
     * Check if API key is configured
     */
    public boolean isConfigured() {
        return unsplashApiKey != null &&
                !unsplashApiKey.isBlank() &&
                !"your-unsplash-api-key".equals(unsplashApiKey);
    }


}
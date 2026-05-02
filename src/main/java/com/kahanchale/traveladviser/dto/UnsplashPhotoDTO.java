package com.kahanchale.traveladviser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnsplashPhotoDTO {
    private String id;
    private String description;
    private String alt_description;
    
    private UnsplashUrls urls;
    private UnsplashUser user;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnsplashUrls {
        private String raw;
        private String full;
        private String regular;
        private String small;
        private String thumb;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnsplashUser {
        private String username;
        private String name;
    }
}

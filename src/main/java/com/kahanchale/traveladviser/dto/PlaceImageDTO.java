package com.kahanchale.traveladviser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImageDTO {
    private Long id;
    private Long placeId;
    private String imageUrl;        // Base64 or CDN URL
    private String contentType;
    private boolean isPrimary;
    private String imageName;
    private Long imageSize;
    private String source;          // "UPLOAD", "UNSPLASH", "S3"
    private String sourceUrl;       // Original source
    private String createdAt;
}

package com.kahanchale.traveladviser.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {
    private String imageData;       // Base64 encoded image
    private String imageName;       // Optional filename
    private boolean setPrimary;     // Whether to set as primary image

    // Compatibility accessor for service code expecting isPrimary()
    public boolean isPrimary() {
        return setPrimary;
    }
}

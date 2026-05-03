package com.kahanchale.traveladviser.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPlacesRequest {
    private Double latitude;
    private Double longitude;
    private Integer radiusInKm = 5; // Default 5km
    private Integer limit = 10;
}

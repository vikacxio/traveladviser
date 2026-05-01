package com.kahanchale.traveladviser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchPlacesRequest {
    private String city;
    private String category;
    private Double budget; // Max price level
    private Double minRating = 0.0;
    private Integer limit = 10;
}

package com.kahanchale.traveladviser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private Double latitude;
    private Double longitude;
    private Double avgRating;
    private Integer totalRatings;
    private Integer priceLevel;
    private String bestSeason;
    private String city;
    private String state;
    private String country;
    private Set<String> tags;
    private Long primaryImageId;
    private String primaryImageSourceUrl;
    private String primaryImageDownloadUrl;
    private List<PlaceImageDTO> images;
}

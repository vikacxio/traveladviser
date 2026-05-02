package com.kahanchale.traveladviser.service;

import com.kahanchale.traveladviser.dto.PlaceDTO;
import com.kahanchale.traveladviser.dto.PlaceImageDTO;
import com.kahanchale.traveladviser.dto.WeatherDto;
import com.kahanchale.traveladviser.entity.Place;
import com.kahanchale.traveladviser.entity.PlaceImage;
import com.kahanchale.traveladviser.entity.Tag;
import com.kahanchale.traveladviser.repository.PlaceImageRepository;
import com.kahanchale.traveladviser.repository.PlaceRepository;
import com.kahanchale.traveladviser.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceImageRepository placeImageRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private WeatherService weatherService;

    /**
     * Get recommendations based on weather at given location
     * Considers temperature, humidity, weather condition for intelligent recommendations
     */
    public List<PlaceDTO> getWeatherBasedRecommendations(Double latitude, Double longitude, Integer limit) {
        // Get weather for location
        WeatherDto weatherResponseDto = weatherService.getWeatherDetails(latitude,longitude);
        List<String> recommendedTags = mapWeatherToTags(weatherResponseDto.getTemperature(), weatherResponseDto.getWeatherCondition(), weatherResponseDto.getHumidity());

        // Get places with these tags
        return getPlacesByTags(recommendedTags, latitude, longitude, limit);
    }

    /**
     * Map weather conditions to place tags
     * Includes temperature, weather condition, and humidity analysis
     */
    private List<String> mapWeatherToTags(int temperature, String weather, Double humidity) {
        List<String> tags = new ArrayList<>();

        // Temperature-based tags
        if (temperature > 30) {
            tags.add("cool_place");
            tags.add("water_spot");
        } else if (temperature < 15) {
            tags.add("snow");
            tags.add("fireplace");
        }
        else {
            tags.add("snow");
            tags.add("cool_place");
        }


        // Humidity-based tags
        if (humidity != null && humidity > 70) {
            tags.add("indoor");  // High humidity - suggest indoor places
        }

        // Weather-based tags
        if (weather != null && weather.toLowerCase().contains("rain")) {
            tags.add("indoor");
            tags.add("museum");
            tags.add("cafe");
        } else if (weather != null && weather.toLowerCase().contains("clear") || weather.toLowerCase().contains("sunny")) {
            tags.add("outdoor");
            tags.add("beach");
            tags.add("hiking");
        }

        return tags;
    }

    /**
     * Get places with specific tags
     */
    private List<PlaceDTO> getPlacesByTags(List<String> tagNames, Double latitude, Double longitude, Integer limit) {
        Set<Place> resultPlaces = new HashSet<>();

        for (String tagName : tagNames) {
            Optional<Tag> tag = tagRepository.findByName(tagName);
            if (tag.isPresent()) {
                // Find all places with this tag
                // Note: You might want to add a custom query method for this
                //if tag is present search the top 20 place on the basis of tags


                //search the top 20 place which contains the tag and in the range of latitude and logitude
                List<Place> places = placeRepository.findAll().stream()
                        .filter(p -> p.getTags() != null && p.getTags().contains(tag.get()))
                        .toList();
                resultPlaces.addAll(places);
            }
        }

        return resultPlaces.stream()
                .sorted((p1, p2) -> Double.compare(p2.getAvgRating(), p1.getAvgRating()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trending places (high rating, recent)
     */
    public List<PlaceDTO> getTrendingPlaces(Integer limit) {
        return placeRepository.findAll().stream()
                .sorted((p1, p2) -> {
                    // Sort by rating then by recency
                    int ratingCompare = Double.compare(p2.getAvgRating(), p1.getAvgRating());
                    if (ratingCompare != 0) return ratingCompare;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PlaceDTO convertToDTO(Place place) {
        PlaceDTO dto = new PlaceDTO();
        dto.setId(place.getId());
        dto.setName(place.getName());
        dto.setDescription(place.getDescription());
        dto.setCategoryName(place.getCategory() != null ? place.getCategory().getName() : null);
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        dto.setAvgRating(place.getAvgRating());
        dto.setTotalRatings(place.getTotalRatings());
        dto.setPriceLevel(place.getPriceLevel());
        dto.setBestSeason(place.getBestSeason());
        dto.setCity(place.getCity());
        dto.setState(place.getState());
        dto.setCountry(place.getCountry());
        if (place.getTags() != null) {
            dto.setTags(place.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toSet()));
        }

        List<PlaceImage> images = placeImageRepository.findByPlaceId(place.getId());
        if (!images.isEmpty()) {
            dto.setImages(images.stream().map(image -> {
                PlaceImageDTO imageDTO = new PlaceImageDTO();
                imageDTO.setId(image.getId());
                imageDTO.setPlaceId(place.getId());
                imageDTO.setImageUrl(image.getImageUrl());
                imageDTO.setContentType(image.getContentType());
                imageDTO.setPrimary(image.isPrimary());
                imageDTO.setImageName(image.getImageName());
                imageDTO.setImageSize(image.getImageSize());
                imageDTO.setSource(image.getSource());
                imageDTO.setSourceUrl(image.getSourceUrl());
                imageDTO.setCreatedAt(image.getCreatedAt() != null ? image.getCreatedAt().toString() : null);
                return imageDTO;
            }).collect(Collectors.toList()));

            PlaceImage primaryImage = images.stream()
                    .filter(PlaceImage::isPrimary)
                    .findFirst()
                    .orElse(images.get(0));
            dto.setPrimaryImageId(primaryImage.getId());
            dto.setPrimaryImageSourceUrl(primaryImage.getSourceUrl());
            dto.setPrimaryImageDownloadUrl("/api/places/" + place.getId() + "/images/" + primaryImage.getId() + "/download");
        }

        return dto;
    }
}

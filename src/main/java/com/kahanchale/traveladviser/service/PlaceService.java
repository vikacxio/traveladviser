package com.kahanchale.traveladviser.service;

import com.kahanchale.traveladviser.dto.PlaceDTO;
import com.kahanchale.traveladviser.dto.PlaceImageDTO;
import com.kahanchale.traveladviser.response.SearchPlacesRequest;
import com.kahanchale.traveladviser.entity.Place;
import com.kahanchale.traveladviser.entity.PlaceImage;
import com.kahanchale.traveladviser.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    public List<PlaceDTO> getNearbyPlaces(Double latitude, Double longitude, Integer radiusKm, Integer limit) {
        int radiusInMeters = radiusKm * 1000;
        List<Place> places = placeRepository.findNearbyPlaces(latitude, longitude, radiusInMeters, limit);
        return places.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<PlaceDTO> searchPlaces(SearchPlacesRequest request) {
        List<Place> places;

        if (request.getCity() != null && !request.getCity().isEmpty()) {
            places = placeRepository.findByCity(request.getCity());
        } else if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            places = placeRepository.findByCategory_Id(Integer.parseInt(request.getCategory()));
        } else {
            places = placeRepository.findAll();
        }

        return places.stream()
                .filter(p -> p.getAvgRating() >= request.getMinRating())
                .filter(p -> request.getBudget() == null || (p.getPriceLevel() != null && p.getPriceLevel() <= request.getBudget()))
                .limit(request.getLimit())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PlaceDTO getPlaceById(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Place not found"));
        return convertToDTO(place);
    }

    public List<PlaceDTO> getPlacesByCity(String city) {
        List<Place> places = placeRepository.findByCity(city);
        return places.stream().map(this::convertToDTO).collect(Collectors.toList());
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

        if (place.getImages() != null) {
            dto.setImages(place.getImages().stream().map(image -> {
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
        }

        PlaceImage primaryImage = place.getPrimaryImage();
        if (primaryImage != null) {
            dto.setPrimaryImageId(primaryImage.getId());
            dto.setPrimaryImageSourceUrl(primaryImage.getSourceUrl());
            dto.setPrimaryImageDownloadUrl("/api/places/" + place.getId() + "/images/" + primaryImage.getId() + "/download");
        }
        return dto;
    }
}

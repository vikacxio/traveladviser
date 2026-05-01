package com.kahanchale.traveladviser.repository;

import com.kahanchale.traveladviser.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // Temporarily using simple distance calculation instead of PostGIS
    @Query(value = "SELECT * FROM places WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) < :radiusInKm " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) " +
            "LIMIT :limit", nativeQuery = true)
    List<Place> findNearbyPlaces(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusInKm") int radiusInKm,
            @Param("limit") int limit
    );

    // Find places by category
    List<Place> findByCategory_Id(Integer categoryId);

    // Find places by city
    List<Place> findByCity(String city);

    // Find places by state
    List<Place> findByState(String state);
}

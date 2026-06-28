package com.kahanchale.traveladviser.repository;

import com.kahanchale.traveladviser.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = "SELECT * FROM places WHERE " +
            "ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusInMeters) " +
            "ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Place> findNearbyPlaces(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusInMeters") double radiusInMeters,
            @Param("limit") int limit
    );

    // Find places by category
    List<Place> findByCategory_Id(Integer categoryId);

    // Find places by city
    List<Place> findByCity(String city);

    // Find places by state
    List<Place> findByState(String state);

    // Find places by name containing the search query
    @Query("SELECT p FROM Place p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.name")
    List<Place> findByNameContaining(String query);

    // Find all places that already have a description
    List<Place> findByDescriptionIsNotNull();
}

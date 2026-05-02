package com.kahanchale.traveladviser.repository;

import com.kahanchale.traveladviser.entity.PlaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    @Query("SELECT pi FROM PlaceImage pi WHERE pi.place.id = :placeId ORDER BY pi.isPrimary DESC, pi.createdAt DESC")
    List<PlaceImage> findByPlaceId(@Param("placeId") Long placeId);

    @Query("SELECT pi FROM PlaceImage pi WHERE pi.place.id = :placeId AND pi.isPrimary = true")
    Optional<PlaceImage> findPrimaryImageByPlaceId(@Param("placeId") Long placeId);

    @Query("SELECT pi FROM PlaceImage pi WHERE pi.place.id = :placeId AND pi.source = :source")
    List<PlaceImage> findByPlaceIdAndSource(@Param("placeId") Long placeId, @Param("source") String source);

    boolean existsByPlaceIdAndSourceUrl(Long placeId, String sourceUrl);

    Optional<PlaceImage> findFirstByPlaceIdOrderByCreatedAtAsc(Long placeId);

    void deleteByPlaceId(Long placeId);

    @Query(value = "SELECT COUNT(*) FROM place_images WHERE place_id = :placeId", nativeQuery = true)
    Integer countByPlaceId(@Param("placeId") Long placeId);
}

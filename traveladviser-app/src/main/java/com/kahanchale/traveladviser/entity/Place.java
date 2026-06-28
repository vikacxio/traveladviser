package com.kahanchale.traveladviser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "places")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column
    private Double avgRating = 0.0;

    @Column
    private Integer totalRatings = 0;

    @Column
    private Integer priceLevel;

    @Column
    private String bestSeason; // SUMMER, WINTER, MONSOON, ALL

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String country;

    @Column
    private String source;

    @Column
    private String sourceId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "place_tags",
            joinColumns = @JoinColumn(name = "place_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    /**
     * One-to-Many relationship with PlaceImage
     * A Place can have multiple images
     */
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PlaceImage> images;

    /**
     * Helper method to get primary image
     */
    @Transient
    public PlaceImage getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(PlaceImage::isPrimary)
                .findFirst()
                .orElse(images.get(0));
    }

    /**
     * Helper method to add image
     */
    public void addImage(PlaceImage image) {
        if (images == null) {
            images = new java.util.ArrayList<>();
        }
        images.add(image);
        image.setPlace(this);
    }

    /**
     * Helper method to remove image
     */
    public void removeImage(PlaceImage image) {
        if (images != null) {
            images.remove(image);
            image.setPlace(null);
        }
    }
}

package com.kahanchale.traveladviser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "place_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(columnDefinition = "BYTEA")
    private byte[] imageData;   // For now - stored in DB

    @Column(columnDefinition = "TEXT")
    private String imageUrl;    // For future - S3 or CDN URL (unbounded length)

    @Column(length = 50)
    private String contentType; // e.g., "image/jpeg", "image/png"

    @Column
    private boolean isPrimary = false; // Primary image for the place

    @Column(length = 500)
    private String imageName;   // Original filename

    @Column
    private Long imageSize;     // Size in bytes

    @Column
    private String source;      // "UPLOAD", "UNSPLASH", "S3"

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;   // Original source URL (e.g., Unsplash URL)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

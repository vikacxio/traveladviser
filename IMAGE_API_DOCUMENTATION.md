# Travel Advisor - Image Management API Documentation

## Overview
This document describes the image management API for the Travel Advisor application. Images can be uploaded manually or fetched automatically from Unsplash API.

## Database Strategy

### Current Implementation (Database Storage)
- Images stored as BYTEA in PostgreSQL
- Suitable for small to medium applications
- Full control over data
- Recommended for < 1000 places with 5-10 images each

### Future Implementation (S3 Storage)
- Images stored in AWS S3
- Direct URLs stored in `imageUrl` column
- Better for large-scale applications
- Reduces database size and improves performance

## Configuration

### 1. Get Unsplash API Key (Optional but Recommended)
```bash
1. Go to https://unsplash.com/oauth/applications
2. Sign in or create an account
3. Create a new application
4. Copy your Access Key
5. Update in application.properties:
   unsplash.api.key=YOUR_UNSPLASH_ACCESS_KEY
```

### 2. Update application.properties
```properties
unsplash.api.key=your-unsplash-api-key
unsplash.api.url=https://api.unsplash.com
```

## API Endpoints

### 1. Upload Image Manually
**Endpoint:** `POST /api/places/{placeId}/images/upload`

**Description:** Upload a Base64 encoded image

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Request Body:**
```json
{
  "imageData": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...",
  "imageName": "taj-mahal-1",
  "setPrimary": true
}
```

**Response:**
```json
{
  "message": "Image uploaded successfully",
  "success": true,
  "image": {
    "id": 1,
    "placeId": 1,
    "imageUrl": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...",
    "contentType": "image/jpeg",
    "isPrimary": true,
    "imageName": "taj-mahal-1",
    "imageSize": 245670,
    "source": "UPLOAD",
    "createdAt": "2026-05-02T10:30:00"
  }
}
```

### 2. Fetch Images from Unsplash
**Endpoint:** `POST /api/places/{placeId}/images/unsplash/fetch`

**Description:** Automatically fetch images from Unsplash using place name as search query

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters:**
- `count` (optional, default: 3) - Number of images to fetch (max 30)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/places/1/images/unsplash/fetch?count=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "Images fetched from Unsplash successfully",
  "success": true,
  "count": 5,
  "images": [
    {
      "id": 2,
      "placeId": 1,
      "contentType": "image/jpeg",
      "isPrimary": false,
      "imageName": "Taj Mahal sunrise",
      "imageSize": 512340,
      "source": "UNSPLASH",
      "sourceUrl": "https://images.unsplash.com/...",
      "createdAt": "2026-05-02T10:35:00"
    }
  ]
}
```

### 3. Get All Images for a Place
**Endpoint:** `GET /api/places/{placeId}/images`

**Response:**
```json
{
  "placeId": 1,
  "count": 6,
  "images": [
    {
      "id": 1,
      "placeId": 1,
      "contentType": "image/jpeg",
      "isPrimary": true,
      "imageName": "taj-mahal-1",
      "imageSize": 245670,
      "source": "UPLOAD",
      "createdAt": "2026-05-02T10:30:00"
    }
  ]
}
```

### 4. Get Primary Image for a Place
**Endpoint:** `GET /api/places/{placeId}/images/primary`

**Response:**
```json
{
  "placeId": 1,
  "image": {
    "id": 1,
    "placeId": 1,
    "imageUrl": "data:image/jpeg;base64,...",
    "isPrimary": true
  }
}
```

### 5. Download Image (Binary)
**Endpoint:** `GET /api/places/{placeId}/images/{imageId}/download`

**Response:** Binary image data with Content-Type: image/jpeg

### 6. Set Primary Image
**Endpoint:** `PUT /api/places/{placeId}/images/{imageId}/set-primary`

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/places/1/images/2/set-primary" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "Image set as primary successfully",
  "success": true,
  "placeId": 1,
  "imageId": 2
}
```

### 7. Delete Image
**Endpoint:** `DELETE /api/places/{placeId}/images/{imageId}`

**Response:**
```json
{
  "message": "Image deleted successfully",
  "success": true,
  "imageId": 1
}
```

### 8. Get Image Count
**Endpoint:** `GET /api/places/{placeId}/images/count`

**Response:**
```json
{
  "placeId": 1,
  "imageCount": 6
}
```

## Complete Workflow Example

### Step 1: Register and Login
```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Step 2: Store JWT Token
```bash
export JWT_TOKEN="eyJhbGciOiJIUzUxMiJ9..."
```

### Step 3: Fetch Unsplash Images
```bash
curl -X POST "http://localhost:8080/api/places/1/images/unsplash/fetch?count=5" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Step 4: View All Images
```bash
curl -X GET "http://localhost:8080/api/places/1/images" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Step 5: Set Primary Image
```bash
curl -X PUT "http://localhost:8080/api/places/1/images/2/set-primary" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Database Schema

### place_images Table
```sql
CREATE TABLE place_images (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT NOT NULL,
    image_data BYTEA,
    image_url TEXT,
    content_type VARCHAR(50),
    is_primary BOOLEAN DEFAULT FALSE,
    image_name VARCHAR(255),
    image_size BIGINT,
    source VARCHAR(50),
    source_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
);

CREATE INDEX idx_place_images_place_id ON place_images(place_id);
CREATE INDEX idx_place_images_is_primary ON place_images(is_primary);
```

## Image Sources

### UPLOAD
- User manually uploads image
- Image stored as Base64 → decoded to BYTEA
- Can set as primary

### UNSPLASH
- Fetched from Unsplash API using place name
- Full image downloaded and stored as BYTEA
- Includes source attribution

### S3 (Future)
- Images stored in AWS S3 bucket
- Only URL stored in database
- Reduces database size
- Better for large-scale deployments

## Performance Tips

1. **Image Size Limits**
   - Set `image.max-size=10485760` (10MB) in properties
   - Compress images before upload

2. **Batch Fetching**
   - Fetch up to 30 images in one request
   - Pagination coming soon

3. **Database Optimization**
   - Add indexes on frequently queried columns
   - Archive old images to S3

4. **Unsplash Rate Limits**
   - Free tier: 50 requests/hour
   - Paid tier: 5000 requests/hour
   - Implement caching for repeated queries

## Future Enhancements

1. **AWS S3 Integration**
   - Direct upload to S3
   - CloudFront CDN caching
   - Cost optimization

2. **Image Processing**
   - Automatic resizing (thumbnail, medium, full)
   - Image compression
   - Format conversion

3. **Advanced Features**
   - Image cropping on upload
   - EXIF data extraction
   - OCR for text extraction
   - AI-based image categorization

4. **Analytics**
   - Track image downloads
   - Popular images per place
   - Usage statistics

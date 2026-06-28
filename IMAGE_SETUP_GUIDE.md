# Image Management - Quick Setup Guide

## 🚀 Quick Start

### 1. Database Migration (Automatic)
The `place_images` table will be created automatically when you start the application (via Hibernate).

### 2. Configure Unsplash API (Optional)

#### Get Free API Key:
1. Visit: https://unsplash.com/oauth/applications
2. Create a new app
3. Copy your **Access Key**

#### Update Configuration:
**File:** `src/main/resources/application.properties`

```properties
# Unsplash API Configuration
unsplash.api.key=YOUR_ACCESS_KEY_HERE
unsplash.api.url=https://api.unsplash.com
```

### 3. Start Application
```bash
cd /Users/vikacxio/Downloads/traveladviser
./mvnw spring-boot:run
```

### 4. Test Image API

#### A. Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Save the token:**
```bash
export JWT="your_token_here"
```

#### B. Fetch Images from Unsplash
```bash
curl -X POST "http://localhost:8080/api/places/1/images/unsplash/fetch?count=3" \
  -H "Authorization: Bearer $JWT"
```

#### C. View All Images
```bash
curl -X GET "http://localhost:8080/api/places/1/images" \
  -H "Authorization: Bearer $JWT" | jq .
```

---

## 📝 Image Workflow

```
Place Created
    ↓
Fetch Images from Unsplash (or Upload Manually)
    ↓
Images Stored in Database (BYTEA)
    ↓
Set Primary Image
    ↓
Display in Frontend (as Base64 or Binary)
    ↓
Future: Migrate to S3 (Just Update imageUrl Column)
```

---

## 🔧 API Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/places/{id}/images/upload` | Upload manual image |
| POST | `/api/places/{id}/images/unsplash/fetch` | Fetch from Unsplash |
| GET | `/api/places/{id}/images` | List all images |
| GET | `/api/places/{id}/images/primary` | Get primary image |
| GET | `/api/places/{id}/images/{imgId}/download` | Download binary |
| PUT | `/api/places/{id}/images/{imgId}/set-primary` | Set as primary |
| DELETE | `/api/places/{id}/images/{imgId}` | Delete image |

---

## 🎯 Complete Flow Example

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"User","email":"user@test.com","password":"pass123"}'

# 2. Login
JWT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"pass123"}' | jq -r '.token')

# 3. Fetch images from Unsplash for place ID 1
curl -X POST "http://localhost:8080/api/places/1/images/unsplash/fetch?count=5" \
  -H "Authorization: Bearer $JWT"

# 4. View all images
curl -X GET "http://localhost:8080/api/places/1/images" \
  -H "Authorization: Bearer $JWT" | jq .

# 5. Get image count
curl -X GET "http://localhost:8080/api/places/1/images/count" \
  -H "Authorization: Bearer $JWT"
```

---

## 📊 Database Details

### Hybrid Storage Design

**Now (Development):**
- Images stored as BYTEA in PostgreSQL
- Full control over data
- Good for testing and small deployments

**Later (Production):**
- Upload to AWS S3
- Store only URL in `image_url` column
- Keep `image_data` as NULL
- Switch frontend to use URLs

**No code changes needed!** Just:
1. Set `imageUrl` in database
2. Front-end checks both `imageData` and `imageUrl`
3. Switch implemented gradually

---

## ⚠️ Important Notes

1. **File Size Limits:**
   - Default max: 10MB per image
   - Modify in `application.properties`

2. **Unsplash Rate Limits:**
   - Free tier: 50 requests/hour
   - Paid tier: 5000 requests/hour

3. **Database Space:**
   - Monitor database size
   - Plan S3 migration for production

4. **Image Compression:**
   - Consider compressing before storage
   - Use image libraries: ImageMagick, GraphicsMagick

---

## 🎨 Using Images in Frontend

### Get Base64 Image (Current)
```bash
curl "http://localhost:8080/api/places/1/images" \
  -H "Authorization: Bearer $JWT"

# Response includes imageUrl with Base64 data
# Display in HTML: <img src="data:image/jpeg;base64,..." />
```

### Download Binary Image
```bash
curl "http://localhost:8080/api/places/1/images/1/download" \
  -H "Authorization: Bearer $JWT" \
  -o place-image.jpg
```

---

## 🔐 Security Considerations

- All image endpoints require JWT authentication
- Images validated for correct MIME types
- File size limits enforced
- Images linked to places (cannot access others' images)

---

## 📈 Future Enhancements

- [ ] Image resizing (thumbnail, medium, full)
- [ ] Image compression on upload
- [ ] S3 integration
- [ ] CDN caching
- [ ] Image search/tagging
- [ ] AI-based recommendations
- [ ] Batch image upload
- [ ] Image cropping tool

---

## 🆘 Troubleshooting

**Issue:** Unsplash images not fetching
- ✅ Check if `unsplash.api.key` is set
- ✅ Verify API key is valid
- ✅ Check rate limits (50 req/hour free)

**Issue:** Image upload fails
- ✅ Check file size (max 10MB)
- ✅ Verify Base64 encoding is correct
- ✅ Check database disk space

**Issue:** Database growing too large
- ✅ Plan migration to S3
- ✅ Archive old images
- ✅ Compress images before upload

---

## 📚 References

- [Unsplash API Docs](https://unsplash.com/documentation)
- [PostgreSQL BYTEA](https://www.postgresql.org/docs/current/datatype-binary.html)
- [AWS S3 Integration Guide](./S3_MIGRATION.md) (Coming soon)

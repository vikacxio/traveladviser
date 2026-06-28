package com.kahanchale.traveladviser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class WikimediaService {

    @Value("${wikimedia.api.url:https://commons.wikimedia.org/w/api.php}")
    private String wikimediaApiUrl;

    private final RestTemplate restTemplate;

    @Value("${wikimedia.user.agent:TravelAdviser/1.0}")
    private String wikimediaUserAgent;

    public WikimediaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Search Wikimedia Commons for images matching a place name.
     */
    @SuppressWarnings("unchecked")
    public List<WikimediaImageData> fetchImagesForPlace(String query, int count) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String url = UriComponentsBuilder
                    .fromUriString(wikimediaApiUrl)
                    .queryParam("action", "query")
                    .queryParam("format", "json")
                    .queryParam("generator", "search")
                    .queryParam("gsrsearch", query)
                    .queryParam("gsrnamespace", 6)
                    .queryParam("gsrlimit", Math.min(Math.max(count, 1), 20))
                    .queryParam("prop", "imageinfo")
                    .queryParam("iiprop", "url|mime|size")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, wikimediaUserAgent);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            if (response == null) {
                return Collections.emptyList();
            }

            Map<String, Object> queryMap = (Map<String, Object>) response.get("query");
            if (queryMap == null) {
                return Collections.emptyList();
            }

            Map<String, Object> pages = (Map<String, Object>) queryMap.get("pages");
            if (pages == null) {
                return Collections.emptyList();
            }

            List<WikimediaImageData> imageDataList = new ArrayList<>();
            for (Object pageObj : pages.values()) {
                if (!(pageObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> page = (Map<String, Object>) pageObj;
                WikimediaImageData imageData = parseWikimediaPage(page);
                if (imageData != null) {
                    imageDataList.add(imageData);
                }
            }

            log.info("Fetched {} Wikimedia image candidates for query: {}", imageDataList.size(), query);
            return imageDataList;
        } catch (Exception e) {
            log.error("Error fetching Wikimedia images for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private WikimediaImageData parseWikimediaPage(Map<String, Object> page) {
        Object titleObj = page.get("title");
        if (!(titleObj instanceof String)) {
            return null;
        }

        Object imageInfoObj = page.get("imageinfo");
        if (!(imageInfoObj instanceof List)) {
            return null;
        }

        List<Object> imageInfoList = (List<Object>) imageInfoObj;
        if (imageInfoList.isEmpty() || !(imageInfoList.get(0) instanceof Map)) {
            return null;
        }

        Map<String, Object> imageInfo = (Map<String, Object>) imageInfoList.get(0);
        Object urlObj = imageInfo.get("url");
        Object mimeObj = imageInfo.get("mime");
        Object sizeObj = imageInfo.get("size");

        if (!(urlObj instanceof String)) {
            return null;
        }

        String url = (String) urlObj;
        if (!url.startsWith("http")) {
            return null;
        }

        String mime = mimeObj instanceof String ? (String) mimeObj : "image/jpeg";
        
        // Only accept image MIME types, skip PDFs and other non-image files
        if (!mime.startsWith("image/")) {
            log.debug("Skipping non-image file type ({}): {}", mime, titleObj);
            return null;
        }

        Long size = null;
        if (sizeObj instanceof Number) {
            size = ((Number) sizeObj).longValue();
        }

        return new WikimediaImageData((String) titleObj, url, mime, size);
    }

    /**
     * Download raw image bytes from Wikimedia Commons.
     */
    public byte[] downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return new byte[0];
        }

        try {
            // Strip query parameters to avoid encoding issues
            String cleanUrl = stripQueryParameters(imageUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, wikimediaUserAgent);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Use URI object to prevent RestTemplate from re-encoding already-encoded URLs
            URI uri = new URI(cleanUrl);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, byte[].class);
            return responseEntity.getBody() != null ? responseEntity.getBody() : new byte[0];
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Wikimedia HTTP error downloading image from URL: {}", imageUrl, e);
            throw e;
        } catch (Exception e) {
            log.error("Error downloading Wikimedia image from URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to download Wikimedia image from URL: " + imageUrl, e);
        }
    }

    private String stripQueryParameters(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        int idx = imageUrl.indexOf('?');
        return idx > 0 ? imageUrl.substring(0, idx) : imageUrl;
    }

    public static class WikimediaImageData {
        private final String title;
        private final String imageUrl;
        private final String mime;
        private final Long size;

        public WikimediaImageData(String title, String imageUrl, String mime, Long size) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.mime = mime;
            this.size = size;
        }

        public String getTitle() {
            return title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getMime() {
            return mime;
        }

        public Long getSize() {
            return size;
        }
    }
}

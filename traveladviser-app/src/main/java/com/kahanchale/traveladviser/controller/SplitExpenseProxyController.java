package com.kahanchale.traveladviser.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
@RequestMapping("/api/split-expense")
public class SplitExpenseProxyController {

    private final RestTemplate restTemplate;
    private final String SPLIT_EXPENSE_BASE_URL = "http://localhost:8081/api/split-expense";

    public SplitExpenseProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String body) {
        try {
            String path = extractPath(request);
            String targetUrl = SPLIT_EXPENSE_BASE_URL + path;

            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            // Copy headers
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.set(headerName, request.getHeader(headerName));
            }

            // Remove host header to avoid issues
            headers.remove("host");

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Proxy error: " + e.getMessage());
        }
    }

    private String extractPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();

        // Remove the proxy prefix
        String path = requestUri.replaceFirst("/api/split-expense", "");

        // Add query parameters if any
        String queryString = request.getQueryString();
        if (queryString != null) {
            path += "?" + queryString;
        }

        return path;
    }
}
package com.kahanchale.traveladviser.security;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitingFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isPreflightRequest(request) || isExcludedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveRateLimitKey(request);

        ConsumptionProbe probe = rateLimitService.tryConsume(key);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setHeader("Retry-After", String.valueOf(waitSeconds));

            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests, please try again later.\"}");
        }
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/error");
    }

    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getName() != null && !authentication.getName().isBlank()) {
            return "user:" + authentication.getName();
        }

        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

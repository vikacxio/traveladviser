package com.kahanchale.traveladviser.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> buckets;
    private final Bandwidth limit;

    public RateLimitService(
            @Value("${rate.limit.capacity:30}") long capacity,
            @Value("${rate.limit.refill-tokens:30}") long refillTokens,
            @Value("${rate.limit.refill-period-seconds:60}") long refillPeriodSeconds
    ) {

        this.limit = Bandwidth.classic(
                capacity,
                Refill.greedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds))
        );

        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public ConsumptionProbe tryConsume(String key) {
        Bucket bucket = buckets.get(key, k ->
                Bucket.builder()
                        .addLimit(limit)
                        .build()
        );

        return bucket.tryConsumeAndReturnRemaining(1);
    }
}
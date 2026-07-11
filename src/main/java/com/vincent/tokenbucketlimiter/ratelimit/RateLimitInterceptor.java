package com.vincent.tokenbucketlimiter.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // thread-safe map
    ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Value("${ratelimit.capacity}")
    private long capacity;

    @Value("${ratelimit.refill-tokens}")
    private long refillTokens;

    @Value("${ratelimit.refill-interval-millis}")
    private long refillIntervalMillis;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // get IP
        String key = request.getRemoteAddr();
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillTokens, refillIntervalMillis));

        if (bucket.tryConsume()) {
            return true;
        }
        // set 429
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        return false;
    }
}

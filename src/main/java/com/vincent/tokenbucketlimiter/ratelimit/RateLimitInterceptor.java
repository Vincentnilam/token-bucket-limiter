package com.vincent.tokenbucketlimiter.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // get IP
        String key = request.getRemoteAddr();
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillTokens, refillIntervalMillis));

        if (bucket.tryConsume()) {
            return true;
        }
        // set 429
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("text/plain;charset=UTF-8");
        // set Retry-After header
        long ms = bucket.millisUntilNextToken();
        long seconds = Math.max(1, (long) Math.ceil(ms / 1000.0));
        response.setHeader("Retry-After", String.valueOf(seconds));

        response.getWriter().write("Rate limit exceeded");


        return false;
    }
}

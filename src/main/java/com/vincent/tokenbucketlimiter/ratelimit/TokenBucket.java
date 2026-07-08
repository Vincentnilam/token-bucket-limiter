package com.vincent.tokenbucketlimiter.ratelimit;

public class TokenBucket {

    private final long capacity;
    private final long refillTokens;
    private final long refillIntervalMillis;

    private double availableTokens;
    private long lastRefillTime;

    public TokenBucket(long capacity, long refillTokens, long refillIntervalMillis) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException("Refill tokens must be greater than 0");
        }
        if (refillIntervalMillis <= 0) {
            throw new IllegalArgumentException("Refill interval must be greater than 0");
        }
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = refillIntervalMillis;
        this.availableTokens = capacity;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refillTokens();
        if (availableTokens < 1) {
            return false;
        }
        availableTokens--;
        return true;
    }

    private void refill() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRefillTime;

        if (elapsedTime <= 0) {
            return;
        }

        double tokensToAdd = ((double) elapsedTime / refillIntervalMillis) * refillTokens;

        availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
        lastRefillTime = currentTime;
    }

    public synchronized double getAvailableTokens() {
        refill();
        return availableTokens;
    }
}

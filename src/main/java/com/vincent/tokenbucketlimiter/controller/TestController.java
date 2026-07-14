package com.vincent.tokenbucketlimiter.controller;

import com.vincent.tokenbucketlimiter.ratelimit.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RateLimit
    @GetMapping("/api/test")
    public String test() {
        return "Request accepted";
    }
}

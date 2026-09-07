package com.hirelens.noteapp.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.ratelimit")
public record RateLimitProperties(Limit login, Limit global) {

    public record Limit(long capacity, Duration window) {
    }
}

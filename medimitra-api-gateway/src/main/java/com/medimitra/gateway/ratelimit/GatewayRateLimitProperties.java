package com.medimitra.gateway.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code medimitra.gateway.rate-limit.*}. */
@ConfigurationProperties(prefix = "medimitra.gateway.rate-limit")
public class GatewayRateLimitProperties {

    private int limitPerWindow = 100;
    private long windowSeconds = 60;

    public int getLimitPerWindow() {
        return limitPerWindow;
    }

    public void setLimitPerWindow(int limitPerWindow) {
        this.limitPerWindow = limitPerWindow;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}

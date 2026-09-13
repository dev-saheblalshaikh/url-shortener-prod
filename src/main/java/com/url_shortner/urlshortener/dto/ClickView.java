package com.url_shortner.urlshortener.dto;

import java.time.LocalDateTime;

public record ClickView(
        String shortCode,
        String originalUrl,
        LocalDateTime clickedAt,
        String ipAddress,
        String userAgent,
        String referrer
) {
}

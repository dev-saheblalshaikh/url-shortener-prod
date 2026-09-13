package com.url_shortner.urlshortener.dto;

import java.time.LocalDateTime;

public record ShortUrlView(
        Long id,
        String originalUrl,
        String shortCode,
        String shortUrl,
        long clickCount,
        boolean active,
        LocalDateTime createdAt
) {
}

package com.url_shortner.urlshortener.dto;

import java.time.LocalDateTime;

public record AdminUserView(
        Long id,
        String email,
        String role,
        boolean blocked,
        LocalDateTime createdAt
) {
}

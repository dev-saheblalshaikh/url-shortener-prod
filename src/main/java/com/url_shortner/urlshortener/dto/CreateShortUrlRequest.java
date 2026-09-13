package com.url_shortner.urlshortener.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class CreateShortUrlRequest {

    @NotBlank(message = "URL is required.")
    @URL(message = "Enter a valid URL, including http:// or https://.")
    @Size(max = 2048, message = "URL is too long.")
    private String originalUrl;

    @Pattern(regexp = "^[A-Za-z0-9_-]{0,32}$", message = "Alias can use letters, numbers, underscores, and hyphens only.")
    private String customAlias;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }
}

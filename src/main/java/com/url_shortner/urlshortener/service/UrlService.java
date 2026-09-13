package com.url_shortner.urlshortener.service;

import com.url_shortner.urlshortener.config.AppProperties;
import com.url_shortner.urlshortener.dto.ClickView;
import com.url_shortner.urlshortener.dto.CreateShortUrlRequest;
import com.url_shortner.urlshortener.dto.DashboardSummary;
import com.url_shortner.urlshortener.dto.ShortUrlView;
import com.url_shortner.urlshortener.model.AppUser;
import com.url_shortner.urlshortener.model.ClickEvent;
import com.url_shortner.urlshortener.model.ShortUrl;
import com.url_shortner.urlshortener.repository.AppUserRepository;
import com.url_shortner.urlshortener.repository.ClickEventRepository;
import com.url_shortner.urlshortener.repository.ShortUrlRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UrlService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final ShortUrlRepository shortUrlRepository;
    private final ClickEventRepository clickEventRepository;
    private final AppUserRepository appUserRepository;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public UrlService(
            ShortUrlRepository shortUrlRepository,
            ClickEventRepository clickEventRepository,
            AppUserRepository appUserRepository,
            AppProperties appProperties
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickEventRepository = clickEventRepository;
        this.appUserRepository = appUserRepository;
        this.appProperties = appProperties;
    }

    @Transactional
    public ShortUrlView createShortUrl(CreateShortUrlRequest request) {
        String shortCode = resolveShortCode(request.getCustomAlias());
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(request.getOriginalUrl().trim());
        shortUrl.setShortCode(shortCode);
        shortUrl.setUser(currentUser());
        return toView(shortUrlRepository.save(shortUrl));
    }

    @Transactional
    public String recordClickAndGetDestination(String shortCode, String ipAddress, String userAgent, String referrer) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .filter(ShortUrl::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Short URL not found."));

        shortUrl.incrementClickCount();

        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setShortUrl(shortUrl);
        clickEvent.setIpAddress(truncate(ipAddress, 64));
        clickEvent.setUserAgent(truncate(userAgent, 512));
        clickEvent.setReferrer(truncate(referrer, 512));
        clickEventRepository.save(clickEvent);

        return shortUrl.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        return new DashboardSummary(
                shortUrlRepository.countByUserEmailIgnoreCase(currentUserEmail()),
                shortUrlRepository.totalClicksForUser(currentUserEmail()),
                shortUrlRepository.countByUserEmailIgnoreCaseAndActiveTrue(currentUserEmail())
        );
    }

    @Transactional(readOnly = true)
    public List<ShortUrlView> findRecentUrls() {
        return shortUrlRepository.findByUserEmailIgnoreCaseOrderByCreatedAtDesc(currentUserEmail(), PageRequest.of(0, 8))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShortUrlView> findTopUrls() {
        return shortUrlRepository.findByUserEmailIgnoreCaseOrderByClickCountDescCreatedAtDesc(currentUserEmail(), PageRequest.of(0, 20))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClickView> findRecentClicks() {
        return clickEventRepository.findRecentClicksForUser(currentUserEmail(), PageRequest.of(0, 20))
                .stream()
                .map(click -> new ClickView(
                        click.getShortUrl().getShortCode(),
                        click.getShortUrl().getOriginalUrl(),
                        click.getClickedAt(),
                        click.getIpAddress(),
                        click.getUserAgent(),
                        click.getReferrer()
                ))
                .toList();
    }

    @Transactional
    public void deleteUrl(Long id) {
        ShortUrl shortUrl = shortUrlRepository.findByIdAndUserEmailIgnoreCase(id, currentUserEmail())
                .orElseThrow(() -> new EntityNotFoundException("Short URL not found."));
        shortUrlRepository.delete(shortUrl);
    }

    private String resolveShortCode(String customAlias) {
        if (StringUtils.hasText(customAlias)) {
            String alias = customAlias.trim();
            if (shortUrlRepository.existsByShortCode(alias)) {
                throw new IllegalArgumentException("That custom alias is already in use.");
            }
            return alias;
        }

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String generated = generateCode();
            if (!shortUrlRepository.existsByShortCode(generated)) {
                return generated;
            }
        }

        throw new IllegalStateException("Could not generate a unique short code. Please try again.");
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    private ShortUrlView toView(ShortUrl shortUrl) {
        String baseUrl = appProperties.getBaseUrl().replaceAll("/+$", "");
        String shortUrlValue = baseUrl + "/" + shortUrl.getShortCode();
        return new ShortUrlView(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrlValue,
                shortUrl.getClickCount(),
                shortUrl.isActive(),
                shortUrl.getCreatedAt()
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private AppUser currentUser() {
        return appUserRepository.findByEmailIgnoreCase(currentUserEmail())
                .orElseThrow(() -> new EntityNotFoundException("Signed-in user not found."));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication is required.");
        }
        AppUser user = appUserRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Signed-in user not found."));
        if (user.isBlocked()) {
            throw new IllegalArgumentException("Your account is blocked.");
        }
        return user.getEmail();
    }
}

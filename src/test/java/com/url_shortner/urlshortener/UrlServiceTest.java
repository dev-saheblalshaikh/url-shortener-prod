package com.url_shortner.urlshortener;

import com.url_shortner.urlshortener.dto.CreateShortUrlRequest;
import com.url_shortner.urlshortener.dto.ShortUrlView;
import com.url_shortner.urlshortener.model.AppUser;
import com.url_shortner.urlshortener.repository.AppUserRepository;
import com.url_shortner.urlshortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UrlServiceTest {

    @Autowired
    private UrlService urlService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createOwner() {
        if (!appUserRepository.existsByEmailIgnoreCase("owner@example.com")) {
            AppUser user = new AppUser();
            user.setEmail("owner@example.com");
            user.setPasswordHash(passwordEncoder.encode("password"));
            appUserRepository.save(user);
        }
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void createsShortUrlWithCustomAliasAndRecordsClick() {
        CreateShortUrlRequest request = new CreateShortUrlRequest();
        request.setOriginalUrl("https://example.com/articles/long-url");
        request.setCustomAlias("docs");

        ShortUrlView created = urlService.createShortUrl(request);
        String destination = urlService.recordClickAndGetDestination("docs", "127.0.0.1", "JUnit", null);

        assertThat(created.shortCode()).isEqualTo("docs");
        assertThat(created.shortUrl()).isEqualTo("http://localhost:8080/docs");
        assertThat(destination).isEqualTo("https://example.com/articles/long-url");
        assertThat(urlService.getSummary().totalClicks()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void rejectsDuplicateCustomAlias() {
        CreateShortUrlRequest first = new CreateShortUrlRequest();
        first.setOriginalUrl("https://example.com/one");
        first.setCustomAlias("same");
        urlService.createShortUrl(first);

        CreateShortUrlRequest second = new CreateShortUrlRequest();
        second.setOriginalUrl("https://example.com/two");
        second.setCustomAlias("same");

        assertThatThrownBy(() -> urlService.createShortUrl(second))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in use");
    }
}

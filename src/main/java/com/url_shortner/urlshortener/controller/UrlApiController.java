package com.url_shortner.urlshortener.controller;

import com.url_shortner.urlshortener.dto.ClickView;
import com.url_shortner.urlshortener.dto.CreateShortUrlRequest;
import com.url_shortner.urlshortener.dto.DashboardSummary;
import com.url_shortner.urlshortener.dto.ShortUrlView;
import com.url_shortner.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UrlApiController {

    private final UrlService urlService;

    public UrlApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/urls")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlView create(@Valid @RequestBody CreateShortUrlRequest request) {
        return urlService.createShortUrl(request);
    }

    @GetMapping("/urls/recent")
    public List<ShortUrlView> recentUrls() {
        return urlService.findRecentUrls();
    }

    @GetMapping("/urls/top")
    public List<ShortUrlView> topUrls() {
        return urlService.findTopUrls();
    }

    @DeleteMapping("/urls/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        urlService.deleteUrl(id);
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummary summary() {
        return urlService.getSummary();
    }

    @GetMapping("/clicks/recent")
    public List<ClickView> recentClicks() {
        return urlService.findRecentClicks();
    }
}

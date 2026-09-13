package com.url_shortner.urlshortener.repository;

import com.url_shortner.urlshortener.model.ShortUrl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    long countByUserEmailIgnoreCase(String email);

    long countByUserEmailIgnoreCaseAndActiveTrue(String email);

    @Query("select coalesce(sum(s.clickCount), 0) from ShortUrl s where lower(s.user.email) = lower(:email)")
    long totalClicksForUser(@Param("email") String email);

    List<ShortUrl> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);

    List<ShortUrl> findByUserEmailIgnoreCaseOrderByClickCountDescCreatedAtDesc(String email, Pageable pageable);

    Optional<ShortUrl> findByIdAndUserEmailIgnoreCase(Long id, String email);
}

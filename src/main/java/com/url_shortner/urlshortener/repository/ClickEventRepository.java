package com.url_shortner.urlshortener.repository;

import com.url_shortner.urlshortener.model.ClickEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("select c from ClickEvent c join fetch c.shortUrl order by c.clickedAt desc")
    List<ClickEvent> findRecentClicks(Pageable pageable);

    @Query("select c from ClickEvent c join fetch c.shortUrl s join fetch s.user where lower(s.user.email) = lower(:email) order by c.clickedAt desc")
    List<ClickEvent> findRecentClicksForUser(@Param("email") String email, Pageable pageable);
}

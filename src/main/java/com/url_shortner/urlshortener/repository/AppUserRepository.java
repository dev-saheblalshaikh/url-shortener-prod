package com.url_shortner.urlshortener.repository;

import com.url_shortner.urlshortener.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(String role);

    List<AppUser> findAllByOrderByCreatedAtDesc();
}

package com.url_shortner.urlshortener.service;

import com.url_shortner.urlshortener.dto.AdminUserView;
import com.url_shortner.urlshortener.model.AppUser;
import com.url_shortner.urlshortener.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AppUserRepository appUserRepository;

    public AdminService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserView> findUsers() {
        return appUserRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public AdminUserView assignAdmin(String email) {
        AppUser user = findUser(email);
        user.setRole("ADMIN");
        return toView(user);
    }

    @Transactional
    public AdminUserView assignUser(String email) {
        AppUser user = findUser(email);
        preventSelfChange(user, "You cannot remove your own admin access.");
        user.setRole("USER");
        return toView(user);
    }

    @Transactional
    public AdminUserView setBlocked(String email, boolean blocked) {
        AppUser user = findUser(email);
        preventSelfChange(user, "You cannot block your own account.");
        user.setBlocked(blocked);
        return toView(user);
    }

    @Transactional
    public void deleteUser(String email) {
        AppUser user = findUser(email);
        preventSelfChange(user, "You cannot delete your own account.");
        appUserRepository.delete(user);
    }

    private AppUser findUser(String email) {
        return appUserRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
    }

    private void preventSelfChange(AppUser user, String message) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException(message);
        }
    }

    private AdminUserView toView(AppUser user) {
        return new AdminUserView(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isBlocked(),
                user.getCreatedAt()
        );
    }
}

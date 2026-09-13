package com.url_shortner.urlshortener.service;

import com.url_shortner.urlshortener.dto.AuthRequest;
import com.url_shortner.urlshortener.dto.AuthUserView;
import com.url_shortner.urlshortener.model.AppUser;
import com.url_shortner.urlshortener.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthUserView register(AuthRequest request, HttpServletRequest servletRequest) {
        String email = normalizeEmail(request.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUserRepository.save(user);

        authenticate(email, request.getPassword(), servletRequest);
        return toView(user);
    }

    public AuthUserView login(AuthRequest request, HttpServletRequest servletRequest) {
        Authentication authentication = authenticate(
                normalizeEmail(request.getEmail()),
                request.getPassword(),
                servletRequest
        );
        return toView((AppUser) authentication.getPrincipal());
    }

    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }

    public AuthUserView currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUser user)) {
            throw new IllegalArgumentException("Not signed in.");
        }
        AppUser freshUser = appUserRepository.findByEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Not signed in."));
        if (freshUser.isBlocked()) {
            throw new IllegalArgumentException("Your account is blocked.");
        }
        return toView(freshUser);
    }

    private Authentication authenticate(String email, String password, HttpServletRequest servletRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        servletRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
        return authentication;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private AuthUserView toView(AppUser user) {
        return new AuthUserView(user.getId(), user.getEmail(), user.getRole());
    }
}

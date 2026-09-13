package com.url_shortner.urlshortener.controller;

import com.url_shortner.urlshortener.dto.AdminUserView;
import com.url_shortner.urlshortener.dto.EmailRequest;
import com.url_shortner.urlshortener.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<AdminUserView> users() {
        return adminService.findUsers();
    }

    @PostMapping("/users/admin")
    public AdminUserView makeAdmin(@Valid @RequestBody EmailRequest request) {
        return adminService.assignAdmin(request.email());
    }

    @PostMapping("/users/user")
    public AdminUserView makeUser(@Valid @RequestBody EmailRequest request) {
        return adminService.assignUser(request.email());
    }

    @PostMapping("/users/block")
    public AdminUserView block(@Valid @RequestBody EmailRequest request) {
        return adminService.setBlocked(request.email(), true);
    }

    @PostMapping("/users/unblock")
    public AdminUserView unblock(@Valid @RequestBody EmailRequest request) {
        return adminService.setBlocked(request.email(), false);
    }

    @DeleteMapping("/users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody EmailRequest request) {
        adminService.deleteUser(request.email());
    }
}

package com.example.skillswap.controller;

import com.example.skillswap.dto.response.AdminDashboardResponseDto;
import com.example.skillswap.dto.response.DashboardResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public DashboardResponseDto getUserDashboard(@PathVariable Long userId, @AuthenticationPrincipal User principal) {
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own dashboard");
        }
        return service.getUserDashboard(userId);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public AdminDashboardResponseDto getAdminDashboard() {

        return service.getAdminDashboard();
    }
}
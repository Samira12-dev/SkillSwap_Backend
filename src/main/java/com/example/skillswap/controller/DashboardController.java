package com.example.skillswap.controller;

import com.example.skillswap.dto.response.AdminDashboardResponseDto;
import com.example.skillswap.dto.response.DashboardResponseDto;
import com.example.skillswap.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/user/{userId}")
    public DashboardResponseDto getUserDashboard(Authentication authentication) {
     String email = authentication.getName();
        return service.getUserDashboard(email);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public AdminDashboardResponseDto getAdminDashboard() {

        return service.getAdminDashboard();
    }
}
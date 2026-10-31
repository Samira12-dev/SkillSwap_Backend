package com.example.skillswap.controller;

import com.example.skillswap.dto.request.BanUserRequestDto;
import com.example.skillswap.dto.request.SuspendUserRequestDto;
import com.example.skillswap.dto.request.UnbanUserRequestDto;
import com.example.skillswap.dto.response.AdminUserDetailsResponseDto;
import com.example.skillswap.dto.response.AdminUserResponseDto;
import com.example.skillswap.dto.response.UserBanResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService service;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDto>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.searchUsers(search, status, pageable));
    }

    @GetMapping("/{userId}")
    public AdminUserDetailsResponseDto getUserDetails(@PathVariable Long userId) {
        return service.getUserDetails(userId);
    }

    @PostMapping("/{userId}/suspend")
    public AdminUserResponseDto suspendUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody SuspendUserRequestDto dto
    ) {
        return service.suspendUser(userId, dto, admin.getEmail());
    }

    @PostMapping("/{userId}/ban")
    public AdminUserResponseDto banUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody BanUserRequestDto dto
    ) {
        return service.banUser(userId, dto, admin.getEmail());
    }

    @PostMapping("/{userId}/unban")
    public AdminUserResponseDto unbanUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User admin,
            @RequestBody(required = false) @Valid UnbanUserRequestDto dto
    ) {
        return service.unbanUser(userId, dto, admin.getEmail());
    }

    @GetMapping("/{userId}/bans")
    public List<UserBanResponseDto> getBanHistory(@PathVariable Long userId) {
        return service.getBanHistory(userId);
    }
}

package com.example.skillswap.controller;

import com.example.skillswap.dto.request.ReportRequestDto;
import com.example.skillswap.dto.request.ReportStatusUpdateRequestDto;
import com.example.skillswap.dto.response.ReportResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.ReportReason;
import com.example.skillswap.enums.ReportStatus;
import com.example.skillswap.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/{reportedUserId}")
    public ResponseEntity<ReportResponseDto> createReport(
            @PathVariable Long reportedUserId,
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody ReportRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createReport(principal.getId(), reportedUserId, dto));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<Page<ReportResponseDto>> getMyReports(
            @AuthenticationPrincipal User principal,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getMyReports(principal.getId(), pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ReportResponseDto>> searchReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.searchReports(status, reason, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReportResponseDto>> getReportsForUser(
            @PathVariable Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getReportsForUser(userId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{reportId}")
    public ReportResponseDto getReport(@PathVariable Long reportId) {
        return service.getReport(reportId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{reportId}/status")
    public ReportResponseDto updateStatus(
            @PathVariable Long reportId,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody ReportStatusUpdateRequestDto dto
    ) {
        return service.updateStatus(reportId, dto, admin.getEmail());
    }
}

package com.example.skillswap.controller;

import com.example.skillswap.dto.response.MatchingResponseDto;
import com.example.skillswap.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    private  final MatchingService service;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<Page<MatchingResponseDto>> findMatiching(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.findMatches(userId, pageable));
    }
    
}
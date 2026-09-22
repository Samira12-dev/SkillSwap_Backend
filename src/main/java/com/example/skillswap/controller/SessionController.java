package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SessionRequestDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;

    @Operation(summary = "Create a session")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public SessionResponseDto createSession(@Valid @RequestBody SessionRequestDto dto,@RequestParam Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only create sessions for yourself");
        }
        return service.createSession(dto,userId);
    }

    @Operation(summary = "Update a session")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{sessionId}")
    public SessionResponseDto updateSession(@PathVariable Long sessionId, @Valid @RequestBody SessionRequestDto dto, @RequestParam Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own sessions");
        }
        return service.updateSession(sessionId, dto,userId);
    }

    @Operation(summary = "Get session by ID")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{sessionId}")
    public SessionResponseDto getSessionById(@PathVariable Long sessionId, @AuthenticationPrincipal User principal){
        return service.getSessionById(sessionId, principal.getId());
    }

    @Operation(summary = "Get sessions by swap")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/swap/{swapId}")
    public ResponseEntity<Page<SessionResponseDto>> getSessionsBySwap(@PathVariable Long swapId, @AuthenticationPrincipal User principal, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getSessionsBySwap(swapId, principal.getId(), pageable));
    }

    @Operation(summary = "Accept session")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{sessionId}/accept/user/{userId}")
    public SessionResponseDto acceptSession(@PathVariable Long sessionId, @PathVariable Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only accept sessions on your own behalf");
        }
        return service.acceptSession(sessionId, userId);
    }

    @Operation(summary = "Cancel session")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{sessionId}/cancel/user/{userId}")
    public SessionResponseDto cancelSession(@PathVariable Long sessionId, @PathVariable Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own sessions");
        }
        return service.cancelSession(sessionId, userId);
    }

    @Operation(summary = "Complete session")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{sessionId}/complete/{userId}")
    public SessionResponseDto completeSession( @PathVariable Long sessionId,@PathVariable Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only complete your own sessions");
        }
        return service.completeSession(sessionId,userId);
    }

    @Operation(summary = "Get my sessions")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<Page<SessionResponseDto>> getMySessions(
            @RequestParam Long userId,
            @AuthenticationPrincipal User principal,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own sessions");
        }
        return ResponseEntity.ok(service.getMySessions(userId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<SessionResponseDto>> getAllSessions(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        return ResponseEntity.ok(service.getAllSessions(pageable));
    }
}
package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SessionRequestDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;

    @Operation(summary = "Create a session")
    @PostMapping
    public SessionResponseDto createSession(@Valid @RequestBody SessionRequestDto dto){
        return service.createSession(dto);
    }

    @Operation(summary = "Update a session")
    @PutMapping("/{sessionId}")
    public SessionResponseDto updateSession(@PathVariable Long sessionId, @Valid @RequestBody SessionRequestDto dto){
        return service.updateSession(sessionId, dto);
    }

    @Operation(summary = "Get session by ID")
    @GetMapping("/{sessionId}")
    public SessionResponseDto getSessionById(@PathVariable Long sessionId){
        return service.getSessionById(sessionId);
    }

    @Operation(summary = "Get sessions by swap")
    @GetMapping("/swap/{swapId}")
    public ResponseEntity<List<SessionResponseDto>> getSessionsBySwap(@PathVariable Long swapId){
        return ResponseEntity.ok(service.getSessionsBySwap(swapId));
    }

    @Operation(summary = "Accept session")
    @PutMapping("/{sessionId}/accept/user/{userId}")
    public SessionResponseDto acceptSession(@PathVariable Long sessionId, @PathVariable Long userId){
        return service.acceptSession(sessionId, userId);
    }

    @Operation(summary = "Cancel session")
    @PutMapping("/{sessionId}/cancel/user/{userId}")
    public SessionResponseDto cancelSession(@PathVariable Long sessionId, @PathVariable Long userId){
        return service.cancelSession(sessionId, userId);
    }

    @Operation(summary = "Complete session")
    @PutMapping("/{sessionId}/complete")
    public SessionResponseDto completeSession( @PathVariable Long sessionId){
        return service.completeSession(sessionId);
    }
}
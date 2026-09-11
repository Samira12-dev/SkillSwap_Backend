package com.example.skillswap.controller;

import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private  final ConversationService service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{swapId}/user/{userId}")
    public ConversationResponseDto createConversation(@PathVariable Long swapId, @PathVariable Long userId) {
        return service.createCoersation(swapId, userId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/user/{userId}")
    public ConversationResponseDto getConversationById(@PathVariable Long id, @PathVariable Long userId){
        return service.getConversationById(id,userId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my/{userId}")
    public ResponseEntity<Page<ConversationResponseDto>>getMyAllConversations(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getMyConversations(userId, pageable));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user1/{userId1}/user2/{userId2}/user/{requestuser}")
    public ConversationResponseDto getConversationBetweenUsers(@PathVariable Long userId1, @PathVariable Long userId2,@PathVariable Long requestuser){
        return service.getConversationBetweenUsers(userId1,userId2,requestuser);
    }

}
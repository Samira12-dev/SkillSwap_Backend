package com.example.skillswap.controller;

import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private  final ConversationService service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{swapId}")
    public ConversationResponseDto createConversation(@PathVariable Long swapId){
        return  service.createCoersation(swapId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ConversationResponseDto getConversationById(@PathVariable Long id){
        return service.getConversationById(id);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my/{userId}")
    public ResponseEntity<List<ConversationResponseDto>>getMyAllConversations(@PathVariable Long userId){
        return ResponseEntity.ok(service.getMyConversations(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user1/{userId1}/user2/{userId2}")
    public ConversationResponseDto getConversationBetweenUsers(@PathVariable Long userId1, @PathVariable Long userId2){
        return service.getConversationBetweenUsers(userId1,userId2);
    }

}

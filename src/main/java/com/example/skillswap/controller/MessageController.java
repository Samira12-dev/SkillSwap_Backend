package com.example.skillswap.controller;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.MessageService;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private  final MessageService  service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{conversationid}/user/{userId}")
    public MessageResponseDto createMessage(@PathVariable Long conversationid,@PathVariable Long userId, @AuthenticationPrincipal User principal, @Valid @RequestBody MessageRequestDto dto){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only send messages as yourself");
        }
        return service.createMessage(dto,conversationid,userId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public MessageResponseDto getMessageById(@PathVariable Long id,@RequestParam Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own messages");
        }
        return service.getMessageById(id,userId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<MessageResponseDto>> getMessagesByConversation(@PathVariable Long conversationId,@RequestParam Long userId, @AuthenticationPrincipal User principal, @PageableDefault(page = 0, size = 10) Pageable pageable){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own messages");
        }
        return ResponseEntity.ok(service.getMessagesByConversation(conversationId,userId,pageable));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id,@RequestParam Long userId, @AuthenticationPrincipal User principal){
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own messages");
        }
        service.deleteMessage(id,userId);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}/read")
    public MessageResponseDto markAsRead(@PathVariable Long id,@RequestParam Long userId, @AuthenticationPrincipal User principal) {
        if (!principal.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own messages");
        }
        return service.markAsRead(id,userId);
    }
}
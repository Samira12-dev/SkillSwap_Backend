package com.example.skillswap.controller;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private  final MessageService  service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{conversationid}/user/{userId}")
    public MessageResponseDto createMessage(@PathVariable Long conversationid,@PathVariable Long userId, @Valid @RequestBody MessageRequestDto dto){
        return service.createMessage(dto,conversationid,userId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public MessageResponseDto getMessageById(@PathVariable Long id){
        return service.getMessageById(id);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponseDto>> getMessagesByConversation(@PathVariable Long conversationId){
        return ResponseEntity.ok(service.getMessagesByConversation(conversationId));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id){
        service.deleteMessage(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}/read")
    public MessageResponseDto markAsRead(@PathVariable Long id) {
        return service.markAsRead(id);
    }
}

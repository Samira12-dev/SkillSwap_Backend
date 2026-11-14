package com.example.skillswap.controller;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversation/{conversationId}")
    public MessageResponseDto sendMessage(
            MessageRequestDto dto,
            @DestinationVariable Long conversationId,
            Principal principal) {

        if (!(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("WebSocket authentication required.");
        }

        return messageService.createMessage(
                dto,
                conversationId,
                user.getId()
        );
    }
}
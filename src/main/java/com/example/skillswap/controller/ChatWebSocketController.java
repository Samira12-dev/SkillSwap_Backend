package com.example.skillswap.controller;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat/{conversationId}/{userId}")
    @SendTo("/topic/conversation/{conversationId}")
    public MessageResponseDto sendMessage(
            MessageRequestDto dto,
            @DestinationVariable Long conversationId,
            @DestinationVariable Long userId) {

        return messageService.createMessage(
                dto,
                conversationId,
                userId
        );
    }
}
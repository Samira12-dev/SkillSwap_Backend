package com.example.skillswap.service;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Message;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.mapper.MessageMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.MessageRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@ExtendWith(MockitoExtension.class)

class MessageServiceTest {
    @Mock
    private MessageRepo messageRepo;
    @Mock
    private  MessageMapper mapper;
    @Mock
    private   ConversationRepo conversationRepo;
    @Mock
    private  UserRepo userRepo;

    @InjectMocks
    private MessageService service;
    @Test
    void createMessage() {
        User sender = new User();
        sender.setId(1L);
        User reciever = new User();
        reciever.setId(2L);

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(reciever);

        Conversation conversation = new Conversation();
        conversation.setId(10L);
        conversation.setSwapRequest(swapRequest);

        MessageRequestDto requestDto = new MessageRequestDto();
        requestDto.setContent("Salam");
        Message message = new Message();
        message.setId(20L);

        MessageResponseDto responseDto = new MessageResponseDto();

        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(conversationRepo.findById(10L)).thenReturn(Optional.of(conversation));

        when(messageRepo.save(any(Message.class))).thenReturn(message);
        when(mapper.toResponse(message)).thenReturn(responseDto);

        MessageResponseDto rsult= service.createMessage(requestDto,10L,1L);
        assertEquals(responseDto, rsult);


    }
    @Test
    void markAsRead() {

        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);

        Conversation conversation = new Conversation();
        conversation.setId(10L);
        conversation.setSwapRequest(swapRequest);

        Message message = new Message();
        message.setId(20L);
        message.setSender(sender);
        message.setConversation(conversation);
        message.setRead(false);

        MessageResponseDto responseDto = new MessageResponseDto();

        when(messageRepo.findById(20L))
                .thenReturn(Optional.of(message));

        when(messageRepo.save(message))
                .thenReturn(message);

        when(mapper.toResponse(message))
                .thenReturn(responseDto);

        MessageResponseDto result =
                service.markAsRead(20L, 2L);

        assertTrue(message.isRead());
        assertEquals(responseDto, result);
    }
}
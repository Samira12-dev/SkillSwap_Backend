package com.example.skillswap.service;

import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SwapStatus;
import com.example.skillswap.mapper.ConversationMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
    @Mock
    private ConversationRepo repo;
    @Mock
    private ConversationMapper mapper;
    @Mock
    private SwapRequestRepo swapRequestRepo;

    @InjectMocks
    private ConversationService service;

    @Test
    void createCoersation() {
        User sender= new User();
        sender.setId(1L);
        User receiver= new User();
        receiver.setId(2L);

        SwapRequest swapRequest =new SwapRequest();
        swapRequest.setId(10L);
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);
        swapRequest.setSwapStatus(SwapStatus.ACCEPTED);

        Conversation conversation =new Conversation();
        conversation.setId(20L);
        conversation.setSwapRequest(swapRequest);

        ConversationResponseDto responseDto= new ConversationResponseDto();

        when(swapRequestRepo.findById(10L)).thenReturn(Optional.of(swapRequest));
        when(repo.findBySwapRequestId(10L)).thenReturn(Optional.empty());

        when(repo.save(any(Conversation.class))).thenReturn(conversation);

        when(mapper.toResponse(conversation)).thenReturn(responseDto);
        ConversationResponseDto result= service.createCoersation(10L,1L);

        assertEquals(responseDto, result);

    }

    @Test
    void getConversationById() {

        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);

        Conversation conversation = new Conversation();
        conversation.setId(20L);
        conversation.setSwapRequest(swapRequest);

        ConversationResponseDto responseDto =
                new ConversationResponseDto();

        when(repo.findById(20L))
                .thenReturn(Optional.of(conversation));

        when(mapper.toResponse(conversation))
                .thenReturn(responseDto);

        ConversationResponseDto result =
                service.getConversationById(20L, 1L);

        assertEquals(responseDto, result);
    }
}
package com.example.skillswap.service;

import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.mapper.SessionMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
     @Mock
     private SessionRepo repo;
     @Mock
     private SessionMapper mapper;
     @Mock
     private UserRepo userRepo;
     @Mock
     private ConversationRepo conversationRepo;

     @InjectMocks
     private  SessionService service;


    @Test
    void acceptSession() {

        User sender =new User();
        sender.setId(2L);

        User receiver =new User();
        receiver.setId(2L);

        SwapRequest swapRequest =new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);

        Conversation conversation=new Conversation();
        conversation.setId(3L);
        conversation.setSwapRequest(swapRequest);

        Session session=new Session();
        session.setId(1L);
        session.setConversation(conversation);
        session.setStatus(SessionStatus.PROPOSED);

        SessionResponseDto responseDto =new SessionResponseDto();

        when(repo.findById(1L)).thenReturn(Optional.of(session));
        when(repo.save(session)).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(responseDto);

        SessionResponseDto result=  service.acceptSession(1L,2L);

        assertEquals(SessionStatus.CONFIRMED,session.getStatus());
        assertEquals(responseDto,result);


    }

    @Test
    void completeSession() {
        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        SwapRequest swapRequest =new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);

        Conversation conversation =new Conversation();
        conversation.setId(3L);
        conversation.setSwapRequest(swapRequest);

        Session session =new Session();
        session.setId(1L);
        session.setConversation(conversation);
        session.setStatus(SessionStatus.CONFIRMED);

        SessionResponseDto responseDto =new SessionResponseDto();
        when(repo.findById(1L)).thenReturn(Optional.of(session));
        when(repo.save(session)).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(responseDto);

        SessionResponseDto result= service.completeSession(1L,2L);

        assertEquals(SessionStatus.COMPLETED,session.getStatus());
        assertEquals(responseDto,result);
    }
}
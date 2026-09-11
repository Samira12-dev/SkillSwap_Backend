package com.example.skillswap.service;

import com.example.skillswap.dto.request.SessionRequestDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.mapper.SessionMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.SessionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private  final SessionRepo sessionRepo;
    private final SessionMapper mapper;
    private final ConversationRepo conversationRepo;


    @Transactional
    public SessionResponseDto createSession(SessionRequestDto requestDto,Long userId){
        Conversation conversation=conversationRepo.findById(requestDto.getConversationId())
                .orElseThrow(()->new RuntimeException("Conversation not found"));

        SwapRequest swapRequest = conversation.getSwapRequest();
        if(!swapRequest.getSender().getId().equals(userId) && !swapRequest.getReceiver().getId().equals(userId)){
            throw new RuntimeException("You are not part of this conversation");

        }
        Session session =new Session();
        session.setDate(requestDto.getDate());
        session.setDuration(requestDto.getDuration());
        session.setMode(requestDto.getMode());
        session.setStatus(SessionStatus.PROPOSED);
        session.setConversation(conversation);

        Session saved=sessionRepo.save(session);
        return mapper.toResponse(saved);
    }

    @Transactional
    public SessionResponseDto updateSession(Long sessionId,SessionRequestDto dto,Long userId){
        Session session=sessionRepo.findById(sessionId).orElseThrow(()->new RuntimeException("Session not found"));
        SwapRequest swapRequest = session.getConversation().getSwapRequest();
        if (!swapRequest.getSender().getId().equals(userId)
                && !swapRequest.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this session");
        }
        mapper.updateSession(dto,session);
        return mapper.toResponse(session);
    }

    @Transactional
     public SessionResponseDto getSessionById(Long sessionId){
        Session session=sessionRepo.findById(sessionId).orElseThrow(()->new RuntimeException("Session not found"));
        return mapper.toResponse(session);
    }

     @Transactional
    public List<SessionResponseDto> getSessionsBySwap(Long swapId){
        List<Session> sessions= sessionRepo.findByConversationSwapRequestId(swapId);
        return sessions.stream()
                .map(mapper::toResponse)
                .toList();
     }

     @Transactional
    public SessionResponseDto acceptSession(Long sessionId,Long userId){
        Session session=sessionRepo.findById(sessionId).orElseThrow(()->new RuntimeException("session not found"));
        if(session.getStatus() != SessionStatus.PROPOSED){
            throw new RuntimeException("Session can't be accpeted");
        }
         SwapRequest swapRequest =session.getConversation().getSwapRequest();
        if(!swapRequest.getReceiver().getId().equals(userId)){
            throw  new RuntimeException("You are not allowed to accept this");
        }
      session.setStatus(SessionStatus.CONFIRMED);
        Session saved= sessionRepo.save(session);
        return mapper.toResponse(saved);
     }

     @Transactional
    public SessionResponseDto cancelSession(Long sessionId,Long userId){
        Session session=sessionRepo.findById(sessionId).orElseThrow(()->new RuntimeException("Session not found"));
        if(session.getStatus()== SessionStatus.COMPLETED){
            throw new RuntimeException("Completed session can't be canceled");
        }
        SwapRequest swapRequest= session.getConversation().getSwapRequest();
        if(!swapRequest.getSender().getId().equals(userId) && !swapRequest.getReceiver().getId().equals(userId)){
            throw new RuntimeException("You are not allowed to cancel this session");
        }
        session.setStatus(SessionStatus.CANCELLED);
        Session saved= sessionRepo.save(session);
        return mapper.toResponse(saved);
     }

     @Transactional
    public SessionResponseDto completeSession(Long sessionId,Long userId){
        Session session=sessionRepo.findById(sessionId).orElseThrow(()->new RuntimeException("Session not found"));
         SwapRequest swapRequest = session.getConversation().getSwapRequest();
         if(!swapRequest.getSender().getId().equals(userId) && !swapRequest.getReceiver().getId().equals(userId)){
             throw new RuntimeException("You are not allowed to complete this session");
         }
         if(session.getStatus()!=SessionStatus.CONFIRMED){
            throw  new RuntimeException("Only confirmed sessions can be completed");
        }
        session.setStatus(SessionStatus.COMPLETED);
        Session saved= sessionRepo.save(session);
       return mapper.toResponse(saved);
     }


}

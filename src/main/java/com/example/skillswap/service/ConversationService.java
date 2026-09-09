package com.example.skillswap.service;

import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.enums.SwapStatus;
import com.example.skillswap.mapper.ConversationMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private  final ConversationRepo conversationRepo;
    private  final SwapRequestRepo swapRequestRepo;
    private final ConversationMapper mapper;

    @Transactional
    public ConversationResponseDto createCoersation( Long swapId){
        SwapRequest swapRequest=swapRequestRepo.findById(swapId).orElseThrow(()->new RuntimeException("swap not found"));
        if(swapRequest.getSwapStatus()!= SwapStatus.ACCEPTED){
            throw  new RuntimeException("Conversation can only created if swap accepted");
        }
        if(conversationRepo.findBySwapRequestId(swapId).isPresent()){
            throw new RuntimeException("Conversation already exists");
        }
        Conversation conversation= new Conversation();
        conversation.setSwapRequest(swapRequest);

        Conversation  saved= conversationRepo.save(conversation);
        return  mapper.toResponse(saved);
    }

    @Transactional
    public ConversationResponseDto getConversationById(Long id){
        Conversation conversation=conversationRepo.findById(id).orElseThrow(()->new RuntimeException("Conversation not found"));
        return mapper.toResponse(conversation);
    }

    @Transactional
    public List<ConversationResponseDto> getMyConversations(Long userId){
        return conversationRepo.findBySwapRequestSenderIdAndSwapRequestReceiverId(userId,userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ConversationResponseDto getConversationBetweenUsers(Long user1Id, Long user2Id){
        List<Conversation>conversations =conversationRepo.findAll();
        for (Conversation conversation:conversations){
            SwapRequest swapRequest=conversation.getSwapRequest();
            Long senderId= swapRequest.getSender().getId();
            Long receiverId= swapRequest.getReceiver().getId();

            if(senderId.equals(user1Id) && receiverId.equals(user2Id) || (senderId.equals(user2Id) && receiverId.equals(user1Id))){
                return  new ConversationResponseDto(
                        conversation.getId(),
                        conversation.getCreatedAt(),
                        swapRequest.getId(),
                        senderId,
                        swapRequest.getSender().getFirstName(),
                        receiverId,
                        swapRequest.getReceiver().getFirstName()
                );
            }
        }
        throw new RuntimeException("Conversation not found");
    }


}

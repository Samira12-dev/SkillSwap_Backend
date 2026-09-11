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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class MessageService {
    private final MessageRepo messageRepo;
    private final MessageMapper mapper;
    private  final ConversationRepo conversationRepo;
    private final UserRepo userRepo;

    @Transactional
    public MessageResponseDto createMessage(MessageRequestDto messageRequestDto,Long conversationID,Long userId){
        Conversation conversation =conversationRepo.findById(conversationID).orElseThrow(()->
                new RuntimeException("Conversation not found"));
        SwapRequest swap = conversation.getSwapRequest();
        if(!swap.getSender().getId().equals(userId) && !swap.getReceiver().getId().equals(userId)){
            throw new RuntimeException("You are not part of this conversation");
        }
        User sender= userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));

        Message message =new Message();
        message.setSender(sender);
        message.setContent(messageRequestDto.getContent());
        message.setConversation(conversation);

        Message saved= messageRepo.save(message);
        return  mapper.toResponse(saved);
    }
    @Transactional
    public MessageResponseDto getMessageById(Long id, Long userId) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        SwapRequest swap = message.getConversation().getSwapRequest();
        if (!swap.getSender().getId().equals(userId)
                && !swap.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this conversation");
        }
        return mapper.toResponse(message);
    }

    @Transactional
    public Page<MessageResponseDto> getMessagesByConversation(Long conversationId, Long userId, Pageable pageable) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        SwapRequest swap = conversation.getSwapRequest();
        if (!swap.getSender().getId().equals(userId)
                && !swap.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this conversation");
        }
        return messageRepo.findByConversationId(conversationId, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public void deleteMessage(Long id, Long userId) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own message");
        }
        messageRepo.delete(message);
    }


    @Transactional
    public MessageResponseDto markAsRead(Long id, Long userId) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        SwapRequest swap = message.getConversation().getSwapRequest();
        if (!swap.getSender().getId().equals(userId)
                && !swap.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this conversation");
        }
        message.setRead(true);
        Message saved = messageRepo.save(message);
        return mapper.toResponse(saved);
    }
}
package com.example.skillswap.service;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Message;
import com.example.skillswap.entity.User;
import com.example.skillswap.mapper.MessageMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.MessageRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
       User sender= userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));

        Message message =new Message();
        message.setSender(sender);
        message.setContent(messageRequestDto.getContent());
        message.setConversation(conversation);

        Message saved= messageRepo.save(message);
        return  mapper.toResponse(saved);
    }

    @Transactional
    public MessageResponseDto getMessageById(Long id){
        Message message=messageRepo.findById(id).orElseThrow(()->new RuntimeException("Message not found"));
        return mapper.toResponse(message);
    }

    @Transactional
    public List<MessageResponseDto> getMessagesByConversation(Long conversationId){
        List<Message>messages =messageRepo.findByConversationId(conversationId);

        return messages.stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional
    public void deleteMessage(Long id){
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        messageRepo.delete(message);    }


    @Transactional
    public MessageResponseDto markAsRead(Long id) {

        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setRead(true);

        Message saved = messageRepo.save(message);

        return mapper.toResponse(saved);
    }
}

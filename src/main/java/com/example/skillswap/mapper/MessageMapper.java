package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message toEntity(MessageRequestDto requestDto);

    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.firstName", target = "senderName")
    MessageResponseDto toResponse(Message message);
}

package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.ConversationRequestDto;
import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    Conversation toEntity(ConversationRequestDto requestDto);

    @Mapping(source = "swapRequest.id", target = "swapRequestId")
    @Mapping(source = "swapRequest.sender.id", target = "senderId")
    @Mapping(source = "swapRequest.sender.firstName", target = "senderName")
    @Mapping(source = "swapRequest.receiver.id", target = "receiverId")
    @Mapping(source = "swapRequest.receiver.firstName", target = "receiverName")
    ConversationResponseDto toResponse(Conversation conversation);
}

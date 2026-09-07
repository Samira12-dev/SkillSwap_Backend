package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.ConversationRequestDto;
import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.entity.Conversation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    Conversation toEntity(ConversationRequestDto requestDto);
    ConversationResponseDto toResponse(Conversation conversation);
}

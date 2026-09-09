package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SessionRequestDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    Session toEntity(SessionRequestDto requestDto);

    @Mapping(source = "conversation.id", target = "conversationId")
    SessionResponseDto toResponse(Session session);

    void updateSession(SessionRequestDto dto, @MappingTarget Session session);
}

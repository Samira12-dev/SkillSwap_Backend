package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SessionRequestDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    Session toEntity(SessionRequestDto requestDto);
    SessionResponseDto toResponse(Session session);
}

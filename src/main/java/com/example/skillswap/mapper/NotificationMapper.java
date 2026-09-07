package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.NotificationRequestDto;
import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification toEntity(NotificationRequestDto requestDto);
    NotificationResponseDto toResponse(Notification notification);
}


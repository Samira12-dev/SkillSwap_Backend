package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.NotificationRequestDto;
import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification toEntity(NotificationRequestDto requestDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "userName")
    NotificationResponseDto toResponse(Notification notification);
}

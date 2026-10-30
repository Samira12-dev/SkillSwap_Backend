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
    @Mapping(source = "conversation.swapRequest.id", target = "swapRequestId")
    @Mapping(source = "conversation.swapRequest.sender.id", target = "senderId")
    @Mapping(source = "conversation.swapRequest.sender.firstName", target = "senderName")
    @Mapping(source = "conversation.swapRequest.sender.photo", target = "senderPhoto")
    @Mapping(source = "conversation.swapRequest.receiver.id", target = "receiverId")
    @Mapping(source = "conversation.swapRequest.receiver.firstName", target = "receiverName")
    @Mapping(source = "conversation.swapRequest.receiver.photo", target = "receiverPhoto")
    @Mapping(source = "conversation.swapRequest.skillOffered.name", target = "skillOfferedName")
    @Mapping(source = "conversation.swapRequest.skillWanted.name", target = "skillWantedName")
    SessionResponseDto toResponse(Session session);

    void updateSession(SessionRequestDto dto, @MappingTarget Session session);
}

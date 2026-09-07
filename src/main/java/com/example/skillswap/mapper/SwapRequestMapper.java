package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.entity.SwapRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SwapRequestMapper {

    SwapRequest toEntity(SwapRequestRequestDto requestDto);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.firstName", target = "senderName")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "receiver.firstName", target = "receiverName")
    @Mapping(source = "skillOffered.id", target = "skillOfferedId")
    @Mapping(source = "skillOffered.name", target = "skillOfferedName")
    @Mapping(source = "skillWanted.id", target = "skillWantedId")
    @Mapping(source = "skillWanted.name", target = "skillWantedName")
    @Mapping(source = "conversation.id", target = "conversationId")
    SwapRequestResponseDto toResponse(SwapRequest swapRequest);
}

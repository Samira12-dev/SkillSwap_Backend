package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.entity.SwapRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SwapRequestMapper {

     SwapRequest toEntity (SwapRequestRequestDto requestDto);
     SwapRequestResponseDto toResponse(SwapRequest swapRequest);
}

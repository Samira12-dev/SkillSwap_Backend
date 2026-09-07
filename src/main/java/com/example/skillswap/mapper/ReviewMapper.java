package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.ReviewRequestDto;
import com.example.skillswap.dto.response.ReviewResponseDto;
import com.example.skillswap.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toEntity(ReviewRequestDto requestDto);

    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "reviewer.firstName", target = "reviewerName")
    @Mapping(source = "reviewee.id", target = "revieweeId")
    @Mapping(source = "reviewee.firstName", target = "revieweeName")
    @Mapping(source = "session.id", target = "sessionId")
    ReviewResponseDto toResponse(Review review);
}

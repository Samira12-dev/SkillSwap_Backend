package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.entity.SkillDetails;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface SkillDetailsMapper {

     SkillDetails toEntity(SkillDetailsRequestDto requestDto);
     SkillDetailsResponseDto toResponse (SkillDetails skillDetails);
}

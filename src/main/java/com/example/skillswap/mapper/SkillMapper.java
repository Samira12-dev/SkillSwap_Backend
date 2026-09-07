package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SkillRequestDto;
import com.example.skillswap.dto.response.SkillResponseDto;
import com.example.skillswap.entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toEntity(SkillRequestDto requestDto);
    SkillResponseDto toResponse (Skill skill);
}

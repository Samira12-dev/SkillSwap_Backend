package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SkillRequestDto;
import com.example.skillswap.dto.response.SkillResponseDto;
import com.example.skillswap.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toEntity(SkillRequestDto requestDto);
    SkillResponseDto toResponse (Skill skill);

    void updateSkill(SkillRequestDto requestDto,@MappingTarget Skill skill);
}


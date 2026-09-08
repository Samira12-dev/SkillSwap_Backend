package com.example.skillswap.mapper;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.entity.SkillDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillDetailsMapper {
    SkillDetails toEntity(SkillDetailsRequestDto requestDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "userName")
    @Mapping(source = "skill.id", target = "skillId")
    @Mapping(source = "skill.name", target = "skillName")
    @Mapping(source = "skill.category", target = "category")
    SkillDetailsResponseDto toResponse(SkillDetails skillDetails);
}

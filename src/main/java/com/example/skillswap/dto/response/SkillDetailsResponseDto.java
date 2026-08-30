package com.example.skillswap.dto.response;

import com.example.skillswap.enums.SkillLevel;
import com.example.skillswap.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SkillDetailsResponseDto {
    private Long id;
    private SkillType type;
    private SkillLevel level;
    private Long userId;
    private String userName;
    private Long skillId;
    private String skillName;
}

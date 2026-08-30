package com.example.skillswap.dto.request;

import com.example.skillswap.enums.SkillLevel;
import com.example.skillswap.enums.SkillType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SkillDetailsRequestDto {
    @NotNull(message = "Skill is required")
    private Long skillId;
    @NotNull(message = "Skill type is required")
    private SkillType type;
    @NotNull(message = "Skill level is required")
    private SkillLevel level;
}

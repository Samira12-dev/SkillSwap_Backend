package com.example.skillswap.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SkillRequestDto {
    @NotBlank(message = "Skill name is required")
    private String name;
    @NotBlank(message = "Skill category is required")
    private String category;
}

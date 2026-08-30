package com.example.skillswap.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SwapRequestRequestDto {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Skill offered ID is required")
    private Long skillOfferedId;

    @NotNull(message = "Skill wanted ID is required")
    private Long skillWantedId;

    @NotBlank(message = "Message is required")
    private String message;
}

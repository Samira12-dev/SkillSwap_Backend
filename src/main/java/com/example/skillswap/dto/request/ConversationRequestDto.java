package com.example.skillswap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequestDto {
    @NotNull(message = "Swap request ID is required")
    private Long swapRequestId;
}

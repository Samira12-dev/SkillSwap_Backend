package com.example.skillswap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDto {

    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String content;
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
}

package com.example.skillswap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long conversationId;
    private Long senderId;
    private String senderName;
}

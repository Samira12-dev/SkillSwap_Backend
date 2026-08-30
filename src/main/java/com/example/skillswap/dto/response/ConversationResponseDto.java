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
public class ConversationResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private Long swapRequestId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
}

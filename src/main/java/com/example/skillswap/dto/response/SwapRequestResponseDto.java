package com.example.skillswap.dto.response;

import com.example.skillswap.enums.SwapStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SwapRequestResponseDto {
    private Long id;
    private SwapStatus swapStatus;
    private String message;
    private LocalDateTime createdAt;
    private Long senderId;

    private String senderName;

    private Long receiverId;

    private String receiverName;

    private Long skillOfferedId;

    private String skillOfferedName;

    private Long skillWantedId;

    private String skillWantedName;

    private Long conversationId;
}

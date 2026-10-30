package com.example.skillswap.dto.response;

import com.example.skillswap.enums.SessionMode;
import com.example.skillswap.enums.SessionStatus;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDto {
    private Long id;
    private LocalDateTime date;
    private Integer duration;
    private SessionMode mode;
    private SessionStatus status;
    private Long conversationId;
    private String meetingUrl;
    private Long swapRequestId;
    private Long senderId;
    private String senderName;
    private String senderPhoto;
    private Long receiverId;
    private String receiverName;
    private String receiverPhoto;
    private String skillOfferedName;
    private String skillWantedName;
}

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
}

package com.example.skillswap.dto.response;

import com.example.skillswap.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
}

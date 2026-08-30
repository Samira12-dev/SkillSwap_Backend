package com.example.skillswap.dto.request;

import com.example.skillswap.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    @NotBlank(message = "Notification message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
    @NotNull(message = "User ID is required") private Long userId;
}

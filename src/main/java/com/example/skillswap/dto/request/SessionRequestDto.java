package com.example.skillswap.dto.request;

import com.example.skillswap.enums.SessionMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequestDto {
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDateTime date;
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    private Integer duration;
    @NotNull(message = "Session mode is required")
    private SessionMode mode;
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
}

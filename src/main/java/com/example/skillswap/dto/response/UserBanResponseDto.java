package com.example.skillswap.dto.response;

import com.example.skillswap.enums.BanType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBanResponseDto {
    private Long id;
    private Long userId;
    private Long adminId;
    private String adminName;
    private String reason;
    private BanType banType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime liftedAt;
    private String liftedByName;
}

package com.example.skillswap.dto.response;

import com.example.skillswap.enums.ReportReason;
import com.example.skillswap.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private Long reportedUserId;
    private String reportedUserName;
    private String reportedUserEmail;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private String adminNote;
    private Long handledById;
    private String handledByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime handledAt;
}

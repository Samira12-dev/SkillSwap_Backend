package com.example.skillswap.dto.request;

import com.example.skillswap.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatusUpdateRequestDto {

    @NotNull(message = "Report status is required")
    private ReportStatus status;

    @Size(max = 1000, message = "Admin note must not exceed 1000 characters")
    private String adminNote;
}

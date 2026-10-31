package com.example.skillswap.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class SuspendUserRequestDto {

    @NotBlank(message = "Suspension reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @NotNull(message = "Suspension duration is required")
    @Min(value = 1, message = "Suspension must last at least one day")
    @Max(value = 3650, message = "Suspension cannot exceed 10 years")
    private Integer durationDays;
}

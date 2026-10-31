package com.example.skillswap.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnbanUserRequestDto {

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}

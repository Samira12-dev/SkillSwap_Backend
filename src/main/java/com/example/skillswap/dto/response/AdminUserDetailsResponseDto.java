package com.example.skillswap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsResponseDto {
    private AdminUserResponseDto user;
    private List<ReportResponseDto> reports;
    private List<UserBanResponseDto> banHistory;
}

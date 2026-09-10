package com.example.skillswap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponseDto {

    private int totalUsers;
    private int totalSkills;
    private int totalSwapRequests;
    private int activeSessions;
    private int completedSessions;
}
package com.example.skillswap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {
    private int pendingRequests;
    private int upcomingSessions;
    private int unreadMessages;
    private int notifications;
}
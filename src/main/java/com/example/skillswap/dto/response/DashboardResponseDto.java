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
public class DashboardResponseDto {
    private int pendingRequests;
    private int pendingSentRequests;
    private int pendingReceivedRequests;
    private int upcomingSessions;
    private int unreadMessages;
    private int notifications;
    private int totalSwapRequests;
    private int acceptedRequests;
    private int completedRequests;
    private int rejectedRequests;
    private int cancelledRequests;
    private List<SwapRequestResponseDto> recentRequests;
    private List<SessionResponseDto> upcomingSessionItems;
    private List<NotificationResponseDto> recentNotifications;
}

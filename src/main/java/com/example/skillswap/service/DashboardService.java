package com.example.skillswap.service;

import com.example.skillswap.dto.response.AdminDashboardResponseDto;
import com.example.skillswap.dto.response.DashboardResponseDto;
import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.entity.Message;
import com.example.skillswap.entity.Notification;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.enums.SwapStatus;
import com.example.skillswap.mapper.NotificationMapper;
import com.example.skillswap.mapper.SessionMapper;
import com.example.skillswap.mapper.SwapRequestMapper;
import com.example.skillswap.repository.MessageRepo;
import com.example.skillswap.repository.NotificationRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.SkillRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SwapRequestRepo swapRequestRepo;
    private final SessionRepo sessionRepo;
    private final MessageRepo messageRepo;
    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final SkillRepo skillRepo;
    private final SwapRequestMapper swapRequestMapper;
    private final SessionMapper sessionMapper;
    private final NotificationMapper notificationMapper;

    @Transactional
    public DashboardResponseDto getUserDashboard(Long userId) {
        List<SwapRequest> sentRequests = swapRequestRepo
                .findBySenderId(userId, Pageable.unpaged())
                .getContent();
        List<SwapRequest> receivedRequests = swapRequestRepo
                .findByReceiverId(userId, Pageable.unpaged())
                .getContent();

        List<SwapRequest> allRequests = new ArrayList<>(sentRequests);
        allRequests.addAll(receivedRequests);

        int pendingSent = countRequests(sentRequests, SwapStatus.PENDING);
        int pendingReceived = countRequests(receivedRequests, SwapStatus.PENDING);

        List<Session> allSessions = sessionRepo
                .findByConversationSwapRequestSenderIdOrConversationSwapRequestReceiverId(
                        userId,
                        userId,
                        Pageable.unpaged()
                )
                .getContent();

        LocalDateTime now = LocalDateTime.now();
        List<Session> upcomingSessions = allSessions.stream()
                .filter(session -> session.getDate() != null)
                .filter(session -> session.getDate().isAfter(now))
                .filter(session -> session.getStatus() != SessionStatus.CANCELLED)
                .sorted(Comparator.comparing(Session::getDate))
                .toList();

        int unreadMessages = countUnreadMessages(allRequests, userId);

        List<Notification> notifications = notificationRepo
                .findByUserId(
                        userId,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .getContent();

        List<SwapRequest> recentRequests = allRequests.stream()
                .sorted(Comparator.comparing(
                        SwapRequest::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .toList();

        List<SwapRequestResponseDto> recentRequestDtos = recentRequests.stream()
                .map(swapRequestMapper::toResponse)
                .toList();

        List<SessionResponseDto> upcomingSessionDtos = upcomingSessions.stream()
                .limit(5)
                .map(sessionMapper::toResponse)
                .toList();

        List<NotificationResponseDto> notificationDtos = notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();

        return new DashboardResponseDto(
                pendingSent + pendingReceived,
                pendingSent,
                pendingReceived,
                upcomingSessions.size(),
                unreadMessages,
                (int) notificationRepo.countByUserIdAndIsReadFalse(userId),
                allRequests.size(),
                countRequests(allRequests, SwapStatus.ACCEPTED),
                countRequests(allRequests, SwapStatus.COMPLETED),
                countRequests(allRequests, SwapStatus.REJECTED),
                countRequests(allRequests, SwapStatus.CANCELLED),
                recentRequestDtos,
                upcomingSessionDtos,
                notificationDtos
        );
    }

    @Transactional
    public AdminDashboardResponseDto getAdminDashboard() {
        int totalUsers = (int) userRepo.count();
        int totalSkills = (int) skillRepo.count();
        int totalSwapRequest = (int) swapRequestRepo.count();

        int activeSession = 0;
        int completeSession = 0;

        List<Session> sessionList = sessionRepo.findAll();
        for (Session session : sessionList) {
            if (session.getStatus() == SessionStatus.CONFIRMED
                    || session.getStatus() == SessionStatus.COMPLETION_REQUESTED
                    || session.getStatus() == SessionStatus.COMPLETION_DISPUTED) {
                activeSession++;
            }
            if (session.getStatus() == SessionStatus.COMPLETED) {
                completeSession++;
            }
        }

        return new AdminDashboardResponseDto(
                totalUsers,
                totalSkills,
                totalSwapRequest,
                activeSession,
                completeSession
        );
    }

    private int countRequests(List<SwapRequest> requests, SwapStatus status) {
        return (int) requests.stream()
                .filter(request -> request.getSwapStatus() == status)
                .count();
    }

    private int countUnreadMessages(List<SwapRequest> requests, Long userId) {
        Set<Long> conversationIds = new HashSet<>();

        for (SwapRequest request : requests) {
            if (request.getConversation() != null) {
                conversationIds.add(request.getConversation().getId());
            }
        }

        int unreadMessages = 0;

        for (Long conversationId : conversationIds) {
            List<Message> messages = messageRepo
                    .findByConversationId(conversationId, Pageable.unpaged())
                    .getContent();

            for (Message message : messages) {
                if (!message.isRead() && !message.getSender().getId().equals(userId)) {
                    unreadMessages++;
                }
            }
        }

        return unreadMessages;
    }
}

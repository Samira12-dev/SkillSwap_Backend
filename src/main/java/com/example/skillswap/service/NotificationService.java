package com.example.skillswap.service;

import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.entity.Notification;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.mapper.NotificationMapper;
import com.example.skillswap.repository.NotificationRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepo repo;
    private  final UserRepo userRepo;
    private  final NotificationMapper mapper;


    @Transactional
    public NotificationResponseDto createNotification(Long userId, String message, NotificationType type){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);

        Notification saved= repo.save(notification);
        return mapper.toResponse(saved);
    }

    @Transactional
    public NotificationResponseDto getNotificationById(Long notificationId, Long userId) {
        Notification notification = repo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to see this notification");
        }
        return mapper.toResponse(notification);
    }

    @Transactional
    public Page<NotificationResponseDto> getAllNotifications(Pageable pageable){
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public Page<NotificationResponseDto> getNotificationsByUser(Long userId, Pageable pageable){
        return repo.findByUserId(userId, pageable).map(mapper::toResponse);
    }
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, Long userId){
        Notification notification = repo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to modify this notification");
        }
        notification.setRead(true);
        Notification saved = repo.save(notification);
        return mapper.toResponse(saved);
    }

}
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
import org.springframework.stereotype.Service;

import java.util.List;

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
    public NotificationResponseDto getNotificationById(Long Id){
       Notification notification = repo.findById(Id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.toResponse(notification);
    }

    @Transactional
    public List<NotificationResponseDto> getAllNotifications(){
        return repo.findAll().stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional
    public List<NotificationResponseDto> getNotificationsByUser(Long userId){
        List<Notification> notifications =repo.findByUserId(userId);

        return notifications.stream().map(mapper::toResponse).toList();
    }
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId){
        Notification notification = repo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        Notification saved = repo.save(notification);
        return  mapper.toResponse(saved);
    }

}

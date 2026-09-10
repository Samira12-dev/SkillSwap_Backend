package com.example.skillswap.controller;

import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @PostMapping
    public NotificationResponseDto createNotification(@RequestParam Long userId, @RequestParam String message, @RequestParam NotificationType type){
        return service.createNotification(userId, message, type);
    }

    @GetMapping("/{id}")
    public NotificationResponseDto getNotificationById(@PathVariable Long id){
        return service.getNotificationById(id);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications(){
        return ResponseEntity.ok(service.getAllNotifications());
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>>getNotificationsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(service.getNotificationsByUser(userId));
    }

    @PutMapping("/{notificationId}/read")
    public NotificationResponseDto markAsRead(@PathVariable Long notificationId){
        return service.markAsRead(notificationId);
    }
}

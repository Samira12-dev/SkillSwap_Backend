package com.example.skillswap.controller;

import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public NotificationResponseDto createNotification(@RequestParam Long userId, @RequestParam String message, @RequestParam NotificationType type){
        return service.createNotification(userId, message, type);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public NotificationResponseDto getNotificationById(@PathVariable Long id,@RequestParam Long userId){
        return service.getNotificationById(id,userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getAllNotifications(@PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getAllNotifications(pageable));
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponseDto>>getNotificationsByUser(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getNotificationsByUser(userId, pageable));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{notificationId}/read")
    public NotificationResponseDto markAsRead(@PathVariable Long notificationId,@RequestParam Long userId){
        return service.markAsRead(notificationId,userId);
    }
}
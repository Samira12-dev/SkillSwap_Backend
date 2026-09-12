package com.example.skillswap.service;

import com.example.skillswap.dto.response.NotificationResponseDto;
import com.example.skillswap.entity.Notification;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.mapper.NotificationMapper;
import com.example.skillswap.repository.NotificationRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class NotificationServiceTest {
@Mock
private NotificationRepo repo;
@Mock
private NotificationMapper mapper;
@Mock
private UserRepo userRepo;

@InjectMocks
private NotificationService service;


    @Test
    void createNotification() {
        User user =new User();
        user.setId(1L);

        Notification notification = new Notification();
        notification.setId(10L);

        NotificationResponseDto responseDto =new NotificationResponseDto();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        when(repo.save(any(Notification.class))).thenReturn(notification);
        when(mapper.toResponse(notification)).thenReturn(responseDto);

        NotificationResponseDto result=service.createNotification(1L,"new", NotificationType.NEW_MESSAGE);

        assertEquals(responseDto, result);

    }

    @Test
    void markAsRead() {
        User user = new User();
        user.setId(1L);

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setUser(user);
        notification.setRead(false);

        NotificationResponseDto responseDto =new NotificationResponseDto();

        when(repo.findById(10L)).thenReturn(Optional.of(notification));
        when(repo.save(notification)).thenReturn(notification);

        when(mapper.toResponse(notification)).thenReturn(responseDto);
        NotificationResponseDto  result= service.markAsRead(10L,1L);
        assertTrue(notification.isRead());
        assertEquals(responseDto, result);
    }
}
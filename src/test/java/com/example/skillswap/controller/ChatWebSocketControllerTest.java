package com.example.skillswap.controller;

import com.example.skillswap.dto.request.MessageRequestDto;
import com.example.skillswap.dto.response.MessageResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.Role;
import com.example.skillswap.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private ChatWebSocketController controller;

    @Test
    void sendMessageUsesAuthenticatedPrincipal() {
        User user = new User();
        user.setId(7L);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        MessageRequestDto request = new MessageRequestDto();
        request.setContent("Hello");

        MessageResponseDto response = new MessageResponseDto();

        when(messageService.createMessage(request, 10L, 7L)).thenReturn(response);

        MessageResponseDto result = controller.sendMessage(request, 10L, authentication);

        assertSame(response, result);
        verify(messageService).createMessage(request, 10L, 7L);
    }

    @Test
    void sendMessageWithoutAuthenticationIsRejected() {
        MessageRequestDto request = new MessageRequestDto();
        request.setContent("Hello");

        assertThrows(
                AccessDeniedException.class,
                () -> controller.sendMessage(request, 10L, null)
        );
    }

    @Test
    void sendMessageWithUnexpectedPrincipalIsRejected() {
        MessageRequestDto request = new MessageRequestDto();
        request.setContent("Hello");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                null
        );

        assertThrows(
                AccessDeniedException.class,
                () -> controller.sendMessage(request, 10L, authentication)
        );
    }
}

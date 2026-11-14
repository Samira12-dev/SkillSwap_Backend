package com.example.skillswap.config;

import com.example.skillswap.entity.User;
import com.example.skillswap.enums.Role;
import com.example.skillswap.service.AccountStatusService;
import com.example.skillswap.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AccountStatusService accountStatusService;

    @Mock
    private ConversationService conversationService;

    private WebSocketAuthInterceptor interceptor;
    private User user;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(
                jwtUtils,
                userDetailsService,
                accountStatusService,
                conversationService
        );

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);
    }

    @Test
    void connectWithoutAuthorizationIsRejected() {
        StompHeaderAccessor accessor = connectAccessor(null);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void connectWithInvalidTokenIsRejected() {
        StompHeaderAccessor accessor = connectAccessor("Bearer invalid-token");

        when(jwtUtils.validateJwtToken("invalid-token")).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void connectWithBannedUserIsRejected() {
        StompHeaderAccessor accessor = connectAccessor("Bearer valid-token");

        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserFromToken("valid-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(user);
        when(accountStatusService.isBlocked(user)).thenReturn(true);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void connectWithValidTokenSetsPrincipalAndStoresToken() {
        StompHeaderAccessor accessor = connectAccessor("Bearer valid-token");

        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserFromToken("valid-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(user);
        when(accountStatusService.isBlocked(user)).thenReturn(false);
        when(jwtUtils.isTokenVersionValid("valid-token", user)).thenReturn(true);

        interceptor.preSend(message(accessor), null);

        assertNotNull(accessor.getUser());
        assertEquals(user, ((Authentication) accessor.getUser()).getPrincipal());
        assertEquals(
                "valid-token",
                accessor.getSessionAttributes().get(
                        WebSocketAuthInterceptor.TOKEN_SESSION_ATTRIBUTE
                )
        );
    }

    @Test
    void subscribeWithoutPrincipalIsRejected() {
        StompHeaderAccessor accessor = subscribeAccessor(10L, null);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void subscribeToAnotherUsersConversationIsRejected() {
        StompHeaderAccessor accessor = subscribeAccessor(10L, user);
        mockActiveSession(accessor, "valid-token");

        when(conversationService.isParticipant(10L, 1L)).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void subscribeToOwnConversationIsAllowed() {
        StompHeaderAccessor accessor = subscribeAccessor(10L, user);
        mockActiveSession(accessor, "valid-token");

        when(conversationService.isParticipant(10L, 1L)).thenReturn(true);

        interceptor.preSend(message(accessor), null);

        verify(conversationService).isParticipant(10L, 1L);
    }

    @Test
    void sendToAnotherUsersConversationIsRejected() {
        StompHeaderAccessor accessor = sendAccessor(10L, user);
        mockActiveSession(accessor, "valid-token");

        when(conversationService.isParticipant(10L, 1L)).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void sendToOwnConversationIsAllowed() {
        StompHeaderAccessor accessor = sendAccessor(10L, user);
        mockActiveSession(accessor, "valid-token");

        when(conversationService.isParticipant(10L, 1L)).thenReturn(true);

        interceptor.preSend(message(accessor), null);

        verify(conversationService).isParticipant(10L, 1L);
    }

    @Test
    void expiredSessionTokenIsRejected() {
        StompHeaderAccessor accessor = sendAccessor(10L, user);
        accessor.getSessionAttributes().put(
                WebSocketAuthInterceptor.TOKEN_SESSION_ATTRIBUTE,
                "expired-token"
        );

        when(jwtUtils.validateJwtToken("expired-token")).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );

        verify(userDetailsService, never()).loadUserByUsername("user@example.com");
    }

    @Test
    void revokedSessionTokenIsRejected() {
        StompHeaderAccessor accessor = sendAccessor(10L, user);
        accessor.getSessionAttributes().put(
                WebSocketAuthInterceptor.TOKEN_SESSION_ATTRIBUTE,
                "revoked-token"
        );

        when(jwtUtils.validateJwtToken("revoked-token")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(user);
        when(accountStatusService.isBlocked(user)).thenReturn(false);
        when(jwtUtils.isTokenVersionValid("revoked-token", user)).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    @Test
    void sendToUnexpectedDestinationIsRejected() {
        StompHeaderAccessor accessor = sendAccessor(10L, user);
        accessor.setDestination("/app/chat/10/extra");
        mockActiveSession(accessor, "valid-token");

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message(accessor), null)
        );
    }

    private void mockActiveSession(StompHeaderAccessor accessor, String token) {
        accessor.getSessionAttributes().put(
                WebSocketAuthInterceptor.TOKEN_SESSION_ATTRIBUTE,
                token
        );

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(user);
        when(accountStatusService.isBlocked(user)).thenReturn(false);
        when(jwtUtils.isTokenVersionValid(token, user)).thenReturn(true);
    }

    private StompHeaderAccessor connectAccessor(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());

        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }

        return accessor;
    }

    private StompHeaderAccessor subscribeAccessor(Long conversationId, User principal) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setDestination("/topic/conversation/" + conversationId);

        if (principal != null) {
            accessor.setUser(authentication(principal));
        }

        return accessor;
    }

    private StompHeaderAccessor sendAccessor(Long conversationId, User principal) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setDestination("/app/chat/" + conversationId);
        accessor.setUser(authentication(principal));
        return accessor;
    }

    private Authentication authentication(User principal) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}

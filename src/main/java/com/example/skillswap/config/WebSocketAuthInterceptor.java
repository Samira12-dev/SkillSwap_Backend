package com.example.skillswap.config;

import com.example.skillswap.entity.User;
import com.example.skillswap.service.AccountStatusService;
import com.example.skillswap.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    static final String TOKEN_SESSION_ATTRIBUTE = "skillswap.websocket.token";

    private static final Pattern TOPIC_CONVERSATION_PATTERN =
            Pattern.compile("^/topic/conversation/(\\d+)$");
    private static final Pattern APP_CHAT_PATTERN =
            Pattern.compile("^/app/chat/(\\d+)$");

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final AccountStatusService accountStatusService;
    private final ConversationService conversationService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            authenticate(accessor);
            return message;
        }

        if (command == StompCommand.SUBSCRIBE) {
            User user = requireActiveUser(accessor);
            Long conversationId = extractConversationId(
                    accessor.getDestination(),
                    TOPIC_CONVERSATION_PATTERN
            );

            if (conversationId == null) {
                throw new AccessDeniedException("Subscription destination is not allowed.");
            }

            requireParticipant(conversationId, user);
            return message;
        }

        if (command == StompCommand.SEND) {
            User user = requireActiveUser(accessor);
            Long conversationId = extractConversationId(
                    accessor.getDestination(),
                    APP_CHAT_PATTERN
            );

            if (conversationId == null) {
                throw new AccessDeniedException("Message destination is not allowed.");
            }

            requireParticipant(conversationId, user);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = extractBearerToken(accessor);

        if (token == null || !jwtUtils.validateJwtToken(token)) {
            throw new AccessDeniedException("WebSocket authentication required.");
        }

        User user = loadActiveUser(jwtUtils.getUserFromToken(token));

        if (!jwtUtils.isTokenVersionValid(token, user)) {
            throw new AccessDeniedException("WebSocket session is no longer authenticated.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        accessor.setUser(authentication);

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            sessionAttributes.put(TOKEN_SESSION_ATTRIBUTE, token);
        }
    }

    private User requireActiveUser(StompHeaderAccessor accessor) {
        User principal = getPrincipal(accessor);

        if (principal == null) {
            throw new AccessDeniedException("WebSocket authentication required.");
        }

        String token = getSessionToken(accessor);

        if (token == null || !jwtUtils.validateJwtToken(token)) {
            throw new AccessDeniedException("WebSocket session is no longer authenticated.");
        }

        User user = loadActiveUser(principal.getEmail());

        if (!jwtUtils.isTokenVersionValid(token, user)) {
            throw new AccessDeniedException("WebSocket session is no longer authenticated.");
        }

        return user;
    }

    private User getPrincipal(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication
                && authentication.getPrincipal() instanceof User user) {
            return user;
        }

        return null;
    }

    private User loadActiveUser(String email) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!(userDetails instanceof User user)) {
                throw new AccessDeniedException("WebSocket authentication required.");
            }

            if (accountStatusService.isBlocked(user)) {
                throw new AccessDeniedException("This account is suspended or banned.");
            }

            return user;
        } catch (UsernameNotFoundException ex) {
            throw new AccessDeniedException("WebSocket authentication required.");
        }
    }

    private void requireParticipant(Long conversationId, User user) {
        if (!conversationService.isParticipant(conversationId, user.getId())) {
            throw new AccessDeniedException("You are not part of this conversation.");
        }
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");

        if (authorization == null) {
            authorization = accessor.getFirstNativeHeader("authorization");
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String getSessionToken(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            return null;
        }

        Object token = sessionAttributes.get(TOKEN_SESSION_ATTRIBUTE);
        return token instanceof String value ? value : null;
    }

    private Long extractConversationId(String destination, Pattern pattern) {
        if (destination == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(destination);

        if (!matcher.matches()) {
            return null;
        }

        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

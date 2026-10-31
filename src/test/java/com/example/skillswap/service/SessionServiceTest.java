package com.example.skillswap.service;

import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.mapper.SessionMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock
    private SessionRepo repo;
    @Mock
    private SessionMapper mapper;
    @Mock
    private ConversationRepo conversationRepo;
    @Mock
    private SwapRequestRepo swapRequestRepo;
    @Mock
    private NotificationService notificationService;

    private Clock clock;
    private SessionService service;

    private User sender;
    private User receiver;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        service = new SessionService(
                repo,
                mapper,
                conversationRepo,
                swapRequestRepo,
                notificationService,
                clock
        );

        sender = new User();
        sender.setId(1L);
        sender.setFirstName("Sender");

        receiver = new User();
        receiver.setId(2L);
        receiver.setFirstName("Receiver");

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);

        conversation = new Conversation();
        conversation.setId(3L);
        conversation.setSwapRequest(swapRequest);
    }

    private Session session(SessionStatus status, LocalDateTime date, int duration) {
        Session session = new Session();
        session.setId(10L);
        session.setConversation(conversation);
        session.setStatus(status);
        session.setDate(date);
        session.setDuration(duration);
        return session;
    }

    @Test
    void acceptSession() {
        Session session = session(SessionStatus.PROPOSED, LocalDateTime.of(2026, 10, 30, 13, 0), 60);
        SessionResponseDto responseDto = new SessionResponseDto();

        when(repo.findById(10L)).thenReturn(Optional.of(session));
        when(repo.save(session)).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(responseDto);

        SessionResponseDto result = service.acceptSession(10L, 2L);

        assertEquals(SessionStatus.CONFIRMED, session.getStatus());
        assertEquals(responseDto, result);
    }

    @Test
    void completeSessionRequestsConfirmationInsteadOfCompletingImmediately() {
        Session session = session(SessionStatus.CONFIRMED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);
        SessionResponseDto responseDto = new SessionResponseDto();

        when(repo.findById(10L)).thenReturn(Optional.of(session));
        when(repo.save(any(Session.class))).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(responseDto);

        SessionResponseDto result = service.completeSession(10L, 2L);

        assertEquals(SessionStatus.COMPLETION_REQUESTED, session.getStatus());
        assertEquals(2L, session.getCompletionRequestedBy().getId());
        assertNotNull(session.getCompletionRequestedAt());
        assertEquals(responseDto, result);
    }

    @Test
    void sessionCannotBeCompletedBeforeScheduledEnd() {
        Session session = session(SessionStatus.CONFIRMED, LocalDateTime.of(2026, 10, 30, 13, 0), 60);

        when(repo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.completeSession(10L, 1L));
        assertEquals(SessionStatus.CONFIRMED, session.getStatus());
    }

    @Test
    void nonParticipantCannotCompleteSession() {
        Session session = session(SessionStatus.CONFIRMED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);

        when(repo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.completeSession(10L, 99L));
    }

    @Test
    void cancelledSessionCannotBeCompleted() {
        Session session = session(SessionStatus.CANCELLED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);

        when(repo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.completeSession(10L, 1L));
    }

    @Test
    void duplicateCompletionRequestIsRejected() {
        Session session = session(SessionStatus.COMPLETION_REQUESTED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);

        when(repo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.completeSession(10L, 1L));
    }

    @Test
    void otherParticipantCanConfirmCompletion() {
        Session session = session(SessionStatus.COMPLETION_REQUESTED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);
        session.setCompletionRequestedBy(sender);
        session.setCompletionRequestedAt(LocalDateTime.of(2026, 10, 30, 11, 5));

        when(repo.findById(10L)).thenReturn(Optional.of(session));
        when(repo.save(any(Session.class))).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(new SessionResponseDto());

        service.confirmCompletion(10L, 2L);

        assertEquals(SessionStatus.COMPLETED, session.getStatus());
        assertNotNull(session.getCompletedAt());
    }

    @Test
    void requesterCannotConfirmOwnCompletionRequest() {
        Session session = session(SessionStatus.COMPLETION_REQUESTED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);
        session.setCompletionRequestedBy(sender);

        when(repo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.confirmCompletion(10L, 1L));
        assertEquals(SessionStatus.COMPLETION_REQUESTED, session.getStatus());
    }

    @Test
    void disputeDoesNotMarkSessionCompleted() {
        Session session = session(SessionStatus.COMPLETION_REQUESTED, LocalDateTime.of(2026, 10, 30, 10, 0), 60);
        session.setCompletionRequestedBy(sender);

        when(repo.findById(10L)).thenReturn(Optional.of(session));
        when(repo.save(any(Session.class))).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(new SessionResponseDto());

        service.disputeCompletion(10L, 2L);

        assertEquals(SessionStatus.COMPLETION_DISPUTED, session.getStatus());
        assertNull(session.getCompletedAt());
        assertNotNull(session.getCompletionDisputedAt());
    }
}

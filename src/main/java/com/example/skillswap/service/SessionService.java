package com.example.skillswap.service;

import com.example.skillswap.dto.request.SessionRequestDto;
import com.example.skillswap.dto.response.SessionResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.mapper.SessionMapper;
import com.example.skillswap.repository.ConversationRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import com.example.skillswap.util.SessionTimes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepo sessionRepo;
    private final SessionMapper mapper;
    private final ConversationRepo conversationRepo;
    private final SwapRequestRepo swapRequestRepo;
    private final NotificationService notificationService;
    private final Clock clock;

    @Transactional
    public SessionResponseDto createSession(SessionRequestDto requestDto, Long userId) {
        Conversation conversation = conversationRepo.findById(requestDto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        SwapRequest swapRequest = conversation.getSwapRequest();

        if (!swapRequest.getSender().getId().equals(userId)
                && !swapRequest.getReceiver().getId().equals(userId)) {
            throw new ConflictException("You are not part of this conversation");
        }

        Session session = new Session();
        session.setDate(requestDto.getDate());
        session.setDuration(requestDto.getDuration());
        session.setMode(requestDto.getMode());
        session.setStatus(SessionStatus.PROPOSED);
        session.setMeetingUrl(requestDto.getMeetingUrl());
        session.setConversation(conversation);

        Session saved = sessionRepo.save(session);

        return mapper.toResponse(saved);
    }

    @Transactional
    public SessionResponseDto updateSession(Long sessionId, SessionRequestDto dto, Long userId) {
        Session session = findSession(sessionId);

        assertParticipant(session, userId, "You are not part of this session");

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new ConflictException("A completed session cannot be changed.");
        }

        mapper.updateSession(dto, session);

        return mapper.toResponse(sessionRepo.save(session));
    }

    @Transactional
    public SessionResponseDto getSessionById(Long sessionId, Long userId) {
        Session session = findSession(sessionId);

        assertParticipant(session, userId, "You are not part of this session");

        return mapper.toResponse(session);
    }

    @Transactional
    public Page<SessionResponseDto> getSessionsBySwap(Long swapId, Long userId, Pageable pageable) {
        SwapRequest swapRequest = swapRequestRepo.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));

        if (!swapRequest.getSender().getId().equals(userId)
                && !swapRequest.getReceiver().getId().equals(userId)) {
            throw new ConflictException("You are not part of this swap");
        }

        return sessionRepo.findByConversationSwapRequestId(swapId, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public SessionResponseDto acceptSession(Long sessionId, Long userId) {
        Session session = findSession(sessionId);

        if (session.getStatus() != SessionStatus.PROPOSED) {
            throw new ConflictException("Session can't be accepted");
        }

        SwapRequest swapRequest = session.getConversation().getSwapRequest();

        if (!swapRequest.getReceiver().getId().equals(userId)) {
            throw new ConflictException("You are not allowed to accept this");
        }

        session.setStatus(SessionStatus.CONFIRMED);

        return mapper.toResponse(sessionRepo.save(session));
    }

    @Transactional
    public SessionResponseDto cancelSession(Long sessionId, Long userId) {
        Session session = findSession(sessionId);

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new ConflictException("Completed session can't be canceled");
        }

        assertParticipant(session, userId, "You are not allowed to cancel this session");

        session.setStatus(SessionStatus.CANCELLED);

        return mapper.toResponse(sessionRepo.save(session));
    }

    /**
     * Requests completion. The other participant must confirm it before the
     * session becomes COMPLETED.
     */
    @Transactional
    public SessionResponseDto completeSession(Long sessionId, Long userId) {
        return requestCompletion(sessionId, userId);
    }

    @Transactional
    public SessionResponseDto requestCompletion(Long sessionId, Long userId) {
        Session session = findSession(sessionId);

        assertParticipant(session, userId, "You are not allowed to complete this session");

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new ConflictException("A cancelled session cannot be completed.");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new ConflictException("This session is already completed.");
        }

        if (session.getStatus() == SessionStatus.COMPLETION_REQUESTED) {
            throw new ConflictException("A completion request is already pending.");
        }

        if (session.getStatus() != SessionStatus.CONFIRMED
                && session.getStatus() != SessionStatus.COMPLETION_DISPUTED) {
            throw new ConflictException("Only confirmed sessions can be completed.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime scheduledEnd = SessionTimes.scheduledEnd(session);

        if (scheduledEnd == null) {
            throw new ConflictException("The session schedule is incomplete.");
        }

        if (now.isBefore(scheduledEnd)) {
            throw new ConflictException(
                    "This session cannot be completed before its scheduled end at " + scheduledEnd + "."
            );
        }

        User requester = participantById(session, userId);

        session.setStatus(SessionStatus.COMPLETION_REQUESTED);
        session.setCompletionRequestedAt(now);
        session.setCompletionRequestedBy(requester);
        session.setCompletionDisputedAt(null);
        session.setCompletedAt(null);

        Session saved = sessionRepo.save(session);

        notificationService.createNotification(
                otherParticipantId(session, userId),
                requester.getFirstName() + " requested completion of your session. Please confirm or dispute it.",
                NotificationType.SESSION_COMPLETION_REQUEST
        );

        return mapper.toResponse(saved);
    }

    @Transactional
    public SessionResponseDto confirmCompletion(Long sessionId, Long userId) {
        Session session = findSession(sessionId);

        assertParticipant(session, userId, "You are not allowed to confirm this session");

        if (session.getStatus() != SessionStatus.COMPLETION_REQUESTED) {
            throw new ConflictException("There is no pending completion request for this session.");
        }

        if (session.getCompletionRequestedBy() != null
                && session.getCompletionRequestedBy().getId().equals(userId)) {
            throw new ConflictException(
                    "The participant who requested completion cannot confirm it."
            );
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime scheduledEnd = SessionTimes.scheduledEnd(session);

        if (scheduledEnd == null || now.isBefore(scheduledEnd)) {
            throw new ConflictException("This session cannot be completed before its scheduled end.");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(now);
        session.setCompletionDisputedAt(null);

        Session saved = sessionRepo.save(session);

        notificationService.createNotification(
                otherParticipantId(session, userId),
                participantById(session, userId).getFirstName()
                        + " confirmed the session completion.",
                NotificationType.SESSION_COMPLETED
        );

        return mapper.toResponse(saved);
    }

    @Transactional
    public SessionResponseDto disputeCompletion(Long sessionId, Long userId) {
        Session session = findSession(sessionId);

        assertParticipant(session, userId, "You are not allowed to dispute this session");

        if (session.getStatus() != SessionStatus.COMPLETION_REQUESTED) {
            throw new ConflictException("There is no pending completion request to dispute.");
        }

        if (session.getCompletionRequestedBy() != null
                && session.getCompletionRequestedBy().getId().equals(userId)) {
            throw new ConflictException("The participant who requested completion cannot dispute it.");
        }

        session.setStatus(SessionStatus.COMPLETION_DISPUTED);
        session.setCompletionDisputedAt(LocalDateTime.now(clock));
        session.setCompletedAt(null);

        Session saved = sessionRepo.save(session);

        notificationService.createNotification(
                otherParticipantId(session, userId),
                participantById(session, userId).getFirstName()
                        + " disputed the session completion. The session was not marked as completed.",
                NotificationType.SESSION_COMPLETION_DISPUTED
        );

        return mapper.toResponse(saved);
    }

    @Transactional
    public Page<SessionResponseDto> getMySessions(Long userId, Pageable pageable) {
        return sessionRepo
                .findByConversationSwapRequestSenderIdOrConversationSwapRequestReceiverId(
                        userId,
                        userId,
                        pageable
                )
                .map(mapper::toResponse);
    }

    @Transactional
    public Page<SessionResponseDto> getAllSessions(Pageable pageable) {
        return sessionRepo.findAll(pageable)
                .map(mapper::toResponse);
    }

    private Session findSession(Long sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private void assertParticipant(Session session, Long userId, String message) {
        SwapRequest swapRequest = session.getConversation().getSwapRequest();

        if (!swapRequest.getSender().getId().equals(userId)
                && !swapRequest.getReceiver().getId().equals(userId)) {
            throw new ConflictException(message);
        }
    }

    private User participantById(Session session, Long userId) {
        SwapRequest swapRequest = session.getConversation().getSwapRequest();

        if (swapRequest.getSender().getId().equals(userId)) {
            return swapRequest.getSender();
        }

        if (swapRequest.getReceiver().getId().equals(userId)) {
            return swapRequest.getReceiver();
        }

        throw new ConflictException("You are not a participant in this session");
    }

    private Long otherParticipantId(Session session, Long userId) {
        SwapRequest swapRequest = session.getConversation().getSwapRequest();

        if (swapRequest.getSender().getId().equals(userId)) {
            return swapRequest.getReceiver().getId();
        }

        return swapRequest.getSender().getId();
    }
}

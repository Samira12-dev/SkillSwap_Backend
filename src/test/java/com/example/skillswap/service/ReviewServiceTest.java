package com.example.skillswap.service;

import com.example.skillswap.dto.request.ReviewRequestDto;
import com.example.skillswap.dto.response.ReviewResponseDto;
import com.example.skillswap.entity.Conversation;
import com.example.skillswap.entity.Review;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.mapper.ReviewMapper;
import com.example.skillswap.repository.ReviewRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private UserRepo userRepo;
    @Mock
    private ReviewRepo repo;
    @Mock
    private ReviewMapper mapper;
    @Mock
    private SessionRepo sessionRepo;
    @Mock
    private NotificationService notificationService;

    private ReviewService service;
    private User reviewer;
    private User reviewee;
    private Session session;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        service = new ReviewService(
                sessionRepo,
                repo,
                userRepo,
                mapper,
                notificationService,
                clock
        );

        reviewer = user(1L, "Reviewer");
        reviewee = user(2L, "Reviewee");

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setSender(reviewer);
        swapRequest.setReceiver(reviewee);

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setSwapRequest(swapRequest);

        session = new Session();
        session.setId(10L);
        session.setStatus(SessionStatus.COMPLETED);
        session.setDate(LocalDateTime.of(2026, 10, 30, 10, 0));
        session.setDuration(60);
        session.setConversation(conversation);
    }

    private User user(Long id, String firstName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        return user;
    }

    private ReviewRequestDto dto(Long revieweeId, int rating, String comment) {
        ReviewRequestDto dto = new ReviewRequestDto();
        dto.setRevieweeId(revieweeId);
        dto.setSessionId(10L);
        dto.setRating(rating);
        dto.setComment(comment);
        return dto;
    }

    private void stubUsersAndSession() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(reviewer));
        when(userRepo.findById(2L)).thenReturn(Optional.of(reviewee));
        when(sessionRepo.findById(10L)).thenReturn(Optional.of(session));
    }

    @Test
    void createReviewAfterCompletedSession() {
        ReviewRequestDto requestDto = dto(2L, 5, "good");
        Review review = new Review();
        review.setId(20L);
        ReviewResponseDto responseDto = new ReviewResponseDto();

        stubUsersAndSession();
        when(repo.existsByReviewerIdAndSessionId(1L, 10L)).thenReturn(false);
        when(repo.save(any(Review.class))).thenReturn(review);
        when(repo.findByRevieweeId(2L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));
        when(mapper.toResponse(review)).thenReturn(responseDto);

        ReviewResponseDto result = service.createReview(requestDto, 1L);

        assertEquals(responseDto, result);
    }

    @Test
    void reviewBeforeSessionCompletedIsRejected() {
        session.setStatus(SessionStatus.CONFIRMED);
        stubUsersAndSession();

        assertThrows(ConflictException.class, () -> service.createReview(dto(2L, 5, "good"), 1L));
    }

    @Test
    void reviewBeforeScheduledEndIsRejected() {
        session.setDate(LocalDateTime.of(2026, 10, 30, 13, 0));
        stubUsersAndSession();

        assertThrows(ConflictException.class, () -> service.createReview(dto(2L, 5, "good"), 1L));
    }

    @Test
    void selfReviewIsRejected() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(reviewer));
        when(sessionRepo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.createReview(dto(1L, 5, "good"), 1L));
    }

    @Test
    void nonParticipantCannotReview() {
        User outsider = user(99L, "Outsider");
        when(userRepo.findById(99L)).thenReturn(Optional.of(outsider));
        when(userRepo.findById(1L)).thenReturn(Optional.of(reviewer));
        when(sessionRepo.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(ConflictException.class, () -> service.createReview(dto(1L, 5, "good"), 99L));
    }

    @Test
    void duplicateReviewIsRejected() {
        stubUsersAndSession();
        when(repo.existsByReviewerIdAndSessionId(1L, 10L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createReview(dto(2L, 5, "good"), 1L));
    }

    @Test
    void invalidRatingIsRejected() {
        stubUsersAndSession();
        when(repo.existsByReviewerIdAndSessionId(1L, 10L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> service.createReview(dto(2L, 6, "good"), 1L));
    }

    @Test
    void commentOver1000CharactersIsRejected() {
        stubUsersAndSession();
        when(repo.existsByReviewerIdAndSessionId(1L, 10L)).thenReturn(false);

        assertThrows(
                ConflictException.class,
                () -> service.createReview(dto(2L, 5, "x".repeat(1001)), 1L)
        );
    }

    @Test
    void getMyReviewForSessionReturnsReview() {
        Review review = new Review();
        ReviewResponseDto responseDto = new ReviewResponseDto();

        when(repo.findByReviewerIdAndSessionId(1L, 10L)).thenReturn(Optional.of(review));
        when(mapper.toResponse(review)).thenReturn(responseDto);

        assertTrue(service.getMyReviewForSession(10L, 1L).isPresent());
    }

    @Test
    void calculateAverageRating() {
        Review review1 = new Review();
        review1.setRating(5);

        Review review2 = new Review();
        review2.setRating(4);

        Page<Review> page = new PageImpl<>(List.of(review1, review2));
        when(repo.findByRevieweeId(2L, Pageable.unpaged())).thenReturn(page);

        assertEquals(4.5, service.calculateAverageRating(2L));
    }

    @Test
    void calculateAverageRatingWithoutReviewsReturnsZero() {
        when(repo.findByRevieweeId(2L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(0, service.calculateAverageRating(2L));
    }
}

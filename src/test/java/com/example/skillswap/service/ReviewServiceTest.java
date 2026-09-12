package com.example.skillswap.service;

import com.example.skillswap.dto.request.ReviewRequestDto;
import com.example.skillswap.dto.response.ReviewResponseDto;
import com.example.skillswap.entity.*;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.mapper.ReviewMapper;
import com.example.skillswap.repository.ReviewRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
@InjectMocks
private ReviewService service;

    @Test
    void createReview() {
        User reviewer = new User();
        reviewer.setId(1L);

        User reviewee = new User();
        reviewee.setId(2L);

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setSender(reviewer);
        swapRequest.setReceiver(reviewee);

        Conversation conversation= new Conversation();
        conversation.setId(3L);
        conversation.setSwapRequest(swapRequest);

        Session session =new Session();
        session.setId(10L);
        session.setStatus(SessionStatus.COMPLETED);
        session.setConversation(conversation);

        ReviewRequestDto requestDto =new ReviewRequestDto();
        requestDto.setRevieweeId(2L);
        requestDto.setSessionId(10L);
        requestDto.setRating(5);
        requestDto.setComment("good");

        Review review =new Review();
        review.setId(20L);

        ReviewResponseDto responseDto = new ReviewResponseDto();

        when(userRepo.findById(1L)).thenReturn(Optional.of(reviewer));
        when(userRepo.findById(2L)).thenReturn(Optional.of(reviewee));
        when(sessionRepo.findById(10L)).thenReturn(Optional.of(session));

        when(repo.save(any(Review.class))).thenReturn(review);
        when(mapper.toResponse(review)).thenReturn(responseDto);

        ReviewResponseDto result=service.createReview(requestDto,1L);

        assertEquals(responseDto,result);
    }



    @Test
    void calculateAverageRating() {

        Review review1 =new Review();
        review1.setRating(5);

        Review review2 =new Review();
        review2.setRating(4);

        Review review3 = new Review();
        review3.setRating(3);

        List<Review> reviews= List.of(review1,review2);
        Page<Review> page = new PageImpl<>(reviews);

        when(repo.findByRevieweeId(2L, Pageable.unpaged())).thenReturn(page);
        double result= service.calculateAverageRating(2L);
        assertEquals(4.5,result);
    }
}
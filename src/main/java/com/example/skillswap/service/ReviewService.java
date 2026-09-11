package com.example.skillswap.service;

import com.example.skillswap.dto.request.ReviewRequestDto;
import com.example.skillswap.dto.response.ReviewResponseDto;
import com.example.skillswap.entity.Review;
import com.example.skillswap.entity.Session;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.mapper.ReviewMapper;
import com.example.skillswap.repository.ReviewRepo;
import com.example.skillswap.repository.SessionRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private  final SessionRepo sessionRepo;
    private  final ReviewRepo repo;
    private final UserRepo userRepo;
    private final ReviewMapper mapper;


    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto requestDto, Long reviewerId){
        User reviewer =userRepo.findById(reviewerId)
                .orElseThrow(()->new RuntimeException("Reviewer not found"));
        User reviewee =userRepo.findById(requestDto.getRevieweeId())
                .orElseThrow(()->new RuntimeException("Reviewee not found"));
        Session session =sessionRepo.findById(requestDto.getSessionId())
                .orElseThrow(()->new RuntimeException("session not found"));
        if(session.getStatus() != SessionStatus.COMPLETED){
            throw  new RuntimeException("A review can only be done if the session has been completed");
        }

        SwapRequest swapRequest = session.getConversation().getSwapRequest();
        Long senderId = swapRequest.getSender().getId();
        Long receiverId = swapRequest.getReceiver().getId();

        if (!reviewerId.equals(senderId) && !reviewerId.equals(receiverId)) {
            throw new RuntimeException("You are not a participant in this session");
        }
        if (reviewerId.equals(senderId)) {
            if (!requestDto.getRevieweeId().equals(receiverId)) {
                throw new RuntimeException("You can only review the other participant");
            }
        }

        if (reviewerId.equals(receiverId)) {
            if (!requestDto.getRevieweeId().equals(senderId)) {
                throw new RuntimeException("You can only review the other participant");
            }
        }

        Review review =new Review();
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        review.setSession(session);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);

        Review saved= repo.save(review);
        return mapper.toResponse(saved);
    }
    @Transactional
    public Page<ReviewResponseDto> getReviewsByUser(Long userId, Pageable pageable){
        return repo.findByRevieweeId(userId, pageable).map(mapper::toResponse);
    }
    @Transactional
    public ReviewResponseDto getReviewById(Long reviewId){
        Review review =repo.findById(reviewId).orElseThrow(()->new RuntimeException("Review not found"));
        return mapper.toResponse(review);
    }
    @Transactional
    public  double calculateAverageRating(Long userId){
        List<Review> reviewList = repo.findByRevieweeId(userId, Pageable.unpaged()).getContent();
        if(reviewList.isEmpty()){
            return 0;
        }
        int total =0;
        for (Review review:reviewList){
            total += review.getRating();
        }
        return total /(double)reviewList.size();
    }



}
package com.example.skillswap.controller;

import com.example.skillswap.dto.request.ReviewRequestDto;
import com.example.skillswap.dto.response.ReviewResponseDto;
import com.example.skillswap.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private  final ReviewService service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{userId}")
    public ReviewResponseDto createReview(@PathVariable Long userId,@Valid @RequestBody  ReviewRequestDto dto){
        return service.createReview(dto,userId);
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponseDto>>getReviewsByUser(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getReviewsByUser(userId, pageable));
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{reviewId}")
    public ReviewResponseDto getReviewById(@PathVariable Long reviewId){
        return service.getReviewById(reviewId);
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}/average")
    public double calculateAverageRating (@PathVariable Long userId){
     return service.calculateAverageRating(userId);
    }

}
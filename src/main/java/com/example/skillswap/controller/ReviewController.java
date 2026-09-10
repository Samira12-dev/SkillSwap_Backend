package com.example.skillswap.controller;

import com.example.skillswap.dto.request.ReviewRequestDto;
import com.example.skillswap.dto.response.ReviewResponseDto;
import com.example.skillswap.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private  final ReviewService service;

    @PostMapping
    public ReviewResponseDto createReview(@Valid @RequestBody @RequestParam ReviewRequestDto dto, @PathVariable Long userId){
        return service.createReview(dto,userId);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDto>>getReviewsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(service.getReviewsByUser(userId));
    }
    @GetMapping("/{reviewId}")
    public ReviewResponseDto getReviewById(@PathVariable Long reviewId){
        return service.getReviewById(reviewId);
    }
    @GetMapping("/user/{userId}/average")
    public double calculateAverageRating (@PathVariable Long userId){
     return service.calculateAverageRating(userId);
    }

}

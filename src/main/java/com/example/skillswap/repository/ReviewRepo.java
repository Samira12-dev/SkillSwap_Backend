package com.example.skillswap.repository;

import com.example.skillswap.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepo extends JpaRepository<Review,Long> {
    Page<Review> findByRevieweeId(Long userId, Pageable pageable);
}
package com.example.skillswap.repository;

import com.example.skillswap.entity.Review;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review,Long> {
    List<Review> findByRevieweeId(Long userId);
}

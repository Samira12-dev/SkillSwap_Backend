package com.example.skillswap.repository;

import com.example.skillswap.entity.SwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwapRequestRepo extends JpaRepository<SwapRequest,Long> {
    List<SwapRequest> findByReceiverId(Long receiverId);
    List<SwapRequest> findBySenderId(Long senderId);
}

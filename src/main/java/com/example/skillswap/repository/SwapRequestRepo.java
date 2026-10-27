package com.example.skillswap.repository;

import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.enums.SwapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwapRequestRepo extends JpaRepository<SwapRequest,Long> {
    Page<SwapRequest> findByReceiverId(Long receiverId, Pageable pageable);
    Page<SwapRequest> findBySenderId(Long senderId, Pageable pageable);
    List<SwapRequest> findByReceiverIdAndSwapStatus(Long receiverId, SwapStatus status);
    boolean existsBySkillOfferedId(Long skillId);
    boolean existsBySkillWantedId(Long skillId);
}
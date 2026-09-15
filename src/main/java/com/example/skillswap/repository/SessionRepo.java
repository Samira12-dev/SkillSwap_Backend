package com.example.skillswap.repository;

import com.example.skillswap.entity.Session;
import com.example.skillswap.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepo extends JpaRepository<Session,Long> {
    Page<Session> findByConversationSwapRequestId(Long swapId, Pageable pageable);
    Page<Session> findByConversationSwapRequestSenderIdOrConversationSwapRequestReceiverId(Long senderId, Long receiverId, Pageable pageable);
}
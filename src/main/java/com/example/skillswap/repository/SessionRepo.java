package com.example.skillswap.repository;

import com.example.skillswap.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepo extends JpaRepository<Session,Long> {
    Page<Session> findByConversationSwapRequestId(Long swapId, Pageable pageable);
}
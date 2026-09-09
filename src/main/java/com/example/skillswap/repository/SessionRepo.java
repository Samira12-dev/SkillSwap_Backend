package com.example.skillswap.repository;

import com.example.skillswap.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepo extends JpaRepository<Session,Long> {
    List<Session> findByConversationSwapRequestId(Long swapId);
}

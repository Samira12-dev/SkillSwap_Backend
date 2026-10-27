package com.example.skillswap.repository;

import com.example.skillswap.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepo extends JpaRepository<Message,Long> {
    Page<Message> findByConversationId(Long conversationId, Pageable pageable);
}
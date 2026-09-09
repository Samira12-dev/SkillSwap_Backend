package com.example.skillswap.repository;

import com.example.skillswap.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message,Long> {
    List<Message> findByConversationId(Long conversationId);
}

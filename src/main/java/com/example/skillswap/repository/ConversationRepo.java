package com.example.skillswap.repository;

import com.example.skillswap.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation,Long> {
    Optional<Conversation>findBySwapRequestId(Long swapRequestId);
   Optional<Conversation>findBySwapRequestSenderIdAndSwapRequestReceiverId(Long senderId,Long receiverId);

    Page<Conversation> findBySwapRequestSenderId(Long senderId, Pageable pageable);
    Page<Conversation> findBySwapRequestReceiverId(Long receiverId, Pageable pageable);
}
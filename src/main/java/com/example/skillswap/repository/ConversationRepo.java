package com.example.skillswap.repository;

import com.example.skillswap.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation,Long> {
    Optional<Conversation>findBySwapRequestId(Long swapRequestId);
   Optional<Conversation>findBySwapRequestSenderIdAndSwapRequestReceiverId(Long senderId,Long receiverId);

    List<Conversation> findBySwapRequestSenderId(Long senderId);
    List<Conversation> findBySwapRequestReceiverId(Long receiverId);
}

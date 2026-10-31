package com.example.skillswap.repository;

import com.example.skillswap.entity.AdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminActionRepo extends JpaRepository<AdminAction, Long> {
    Page<AdminAction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<AdminAction> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId, Pageable pageable);
    boolean existsByTargetUserId(Long targetUserId);
}

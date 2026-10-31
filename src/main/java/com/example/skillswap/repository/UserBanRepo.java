package com.example.skillswap.repository;

import com.example.skillswap.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBanRepo extends JpaRepository<UserBan, Long> {

    List<UserBan> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserBan> findByUserIdAndActiveTrue(Long userId);

    Optional<UserBan> findFirstByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    boolean existsByUserId(Long userId);
}

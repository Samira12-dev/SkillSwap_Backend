package com.example.skillswap.repository;

import com.example.skillswap.entity.SkillDetails;
import com.example.skillswap.enums.SkillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillDetailsRepo extends JpaRepository<SkillDetails,Long> {
    Page<SkillDetails> findByUserId(Long userId, Pageable pageable);
    Optional<SkillDetails> findByUserIdAndSkillId(Long userId,Long skillId);
    boolean existsByUserIdAndSkillId(Long userId, Long skillId);
    boolean existsByUserIdAndSkillIdAndType(Long userId, Long skillId, SkillType type);
    boolean existsBySkillId(Long skillId);
}
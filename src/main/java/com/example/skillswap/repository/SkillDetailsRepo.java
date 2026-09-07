package com.example.skillswap.repository;

import com.example.skillswap.entity.SkillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillDetailsRepo extends JpaRepository<SkillDetails,Long> {
    List<SkillDetails> findByUserId(Long userId);
    Optional<SkillDetails> findByUserIdAndSkillId(Long userId,Long skillId);
}

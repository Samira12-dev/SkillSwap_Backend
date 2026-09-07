package com.example.skillswap.repository;

import com.example.skillswap.entity.Skill;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepo extends JpaRepository<Skill,Long> {
    boolean existsByName( String name);
}

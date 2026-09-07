package com.example.skillswap.repository;

import com.example.skillswap.entity.SwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwapRequestRepo extends JpaRepository<SwapRequest,Long> {
}

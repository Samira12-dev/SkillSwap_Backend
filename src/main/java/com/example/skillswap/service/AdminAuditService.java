package com.example.skillswap.service;

import com.example.skillswap.entity.AdminAction;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.AdminActionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminActionRepo adminActionRepo;

    @Transactional
    public void log(User admin, User targetUser, String action, String details) {
        AdminAction entry = new AdminAction();
        entry.setAdmin(admin);
        entry.setTargetUser(targetUser);
        entry.setAction(action);
        entry.setDetails(details);
        adminActionRepo.save(entry);
    }
}

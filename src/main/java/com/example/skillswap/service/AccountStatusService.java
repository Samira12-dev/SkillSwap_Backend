package com.example.skillswap.service;

import com.example.skillswap.entity.User;
import com.example.skillswap.entity.UserBan;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.exception.AccountBlockedException;
import com.example.skillswap.repository.UserBanRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountStatusService {

    private final UserRepo userRepo;
    private final UserBanRepo userBanRepo;
    private final Clock clock;

    @Transactional
    public boolean isBlocked(User user) {
        if (user == null) {
            return true;
        }

        AccountStatus status = user.getAccountStatus() == null
                ? AccountStatus.ACTIVE
                : user.getAccountStatus();

        if (status == AccountStatus.BANNED) {
            return true;
        }

        if (status == AccountStatus.SUSPENDED) {
            UserBan ban = userBanRepo
                    .findFirstByUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId())
                    .orElse(null);

            if (ban == null) {
                reactivate(user);
                return false;
            }

            if (ban.getEndDate() != null && !ban.getEndDate().isAfter(LocalDateTime.now(clock))) {
                ban.setActive(false);
                ban.setLiftedAt(LocalDateTime.now(clock));
                userBanRepo.save(ban);
                reactivate(user);
                return false;
            }

            return true;
        }

        return false;
    }

    @Transactional
    public void assertActive(User user) {
        if (user == null) {
            throw new AccountBlockedException("Account is not available");
        }

        if (isBlocked(user)) {
            if (user.getAccountStatus() == AccountStatus.BANNED) {
                throw new AccountBlockedException("This account has been permanently banned.");
            }

            UserBan ban = userBanRepo
                    .findFirstByUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId())
                    .orElse(null);

            if (ban != null && ban.getEndDate() != null) {
                throw new AccountBlockedException(
                        "This account is suspended until " + ban.getEndDate() + "."
                );
            }

            throw new AccountBlockedException("This account is currently suspended.");
        }
    }

    private void reactivate(User user) {
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepo.save(user);
    }
}

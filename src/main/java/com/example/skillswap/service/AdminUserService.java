package com.example.skillswap.service;

import com.example.skillswap.dto.request.BanUserRequestDto;
import com.example.skillswap.dto.request.SuspendUserRequestDto;
import com.example.skillswap.dto.request.UnbanUserRequestDto;
import com.example.skillswap.dto.response.AdminUserDetailsResponseDto;
import com.example.skillswap.dto.response.AdminUserResponseDto;
import com.example.skillswap.dto.response.ReportResponseDto;
import com.example.skillswap.dto.response.UserBanResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.entity.UserBan;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.BanType;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.Role;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.UserBanRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepo userRepo;
    private final UserBanRepo userBanRepo;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final AdminAuditService adminAuditService;
    private final Clock clock;

    @Transactional
    public Page<AdminUserResponseDto> searchUsers(
            String search,
            AccountStatus status,
            Pageable pageable
    ) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return userRepo.searchUsers(normalizedSearch, status, pageable).map(this::toUserDto);
    }

    @Transactional
    public AdminUserDetailsResponseDto getUserDetails(Long userId) {
        User user = findUser(userId);

        List<ReportResponseDto> reports = reportService
                .getReportsForUser(userId, Pageable.unpaged())
                .getContent();

        List<UserBanResponseDto> bans = userBanRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toBanDto)
                .toList();

        return new AdminUserDetailsResponseDto(toUserDto(user), reports, bans);
    }

    @Transactional
    public AdminUserResponseDto suspendUser(
            Long userId,
            SuspendUserRequestDto dto,
            String adminEmail
    ) {
        User admin = findAdmin(adminEmail);
        User target = findModerationTarget(userId, admin);

        LocalDateTime now = LocalDateTime.now(clock);
        deactivateActiveBans(target, admin, now);

        UserBan ban = new UserBan();
        ban.setUser(target);
        ban.setAdmin(admin);
        ban.setReason(dto.getReason().trim());
        ban.setBanType(BanType.TEMPORARY);
        ban.setStartDate(now);
        ban.setEndDate(now.plusDays(dto.getDurationDays()));
        ban.setActive(true);
        userBanRepo.save(ban);

        target.setAccountStatus(AccountStatus.SUSPENDED);
        userRepo.save(target);

        notificationService.createNotification(
                target.getId(),
                "Your account has been suspended until " + ban.getEndDate() + ".",
                NotificationType.ACCOUNT_SUSPENDED
        );

        adminAuditService.log(
                admin,
                target,
                "SUSPEND_USER",
                "Suspended until " + ban.getEndDate() + ": " + ban.getReason()
        );

        return toUserDto(target);
    }

    @Transactional
    public AdminUserResponseDto banUser(
            Long userId,
            BanUserRequestDto dto,
            String adminEmail
    ) {
        User admin = findAdmin(adminEmail);
        User target = findModerationTarget(userId, admin);

        LocalDateTime now = LocalDateTime.now(clock);
        deactivateActiveBans(target, admin, now);

        UserBan ban = new UserBan();
        ban.setUser(target);
        ban.setAdmin(admin);
        ban.setReason(dto.getReason().trim());
        ban.setBanType(BanType.PERMANENT);
        ban.setStartDate(now);
        ban.setEndDate(null);
        ban.setActive(true);
        userBanRepo.save(ban);

        target.setAccountStatus(AccountStatus.BANNED);
        userRepo.save(target);

        notificationService.createNotification(
                target.getId(),
                "Your account has been permanently banned.",
                NotificationType.ACCOUNT_BANNED
        );

        adminAuditService.log(
                admin,
                target,
                "BAN_USER",
                "Permanent ban: " + ban.getReason()
        );

        return toUserDto(target);
    }

    @Transactional
    public AdminUserResponseDto unbanUser(
            Long userId,
            UnbanUserRequestDto dto,
            String adminEmail
    ) {
        User admin = findAdmin(adminEmail);
        User target = findUser(userId);

        if (target.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new ConflictException("This account is already active.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        deactivateActiveBans(target, admin, now);

        target.setAccountStatus(AccountStatus.ACTIVE);
        userRepo.save(target);

        notificationService.createNotification(
                target.getId(),
                "Your account has been reinstated. You can sign in again.",
                NotificationType.ACCOUNT_UNBANNED
        );

        String reason = dto == null || dto.getReason() == null || dto.getReason().isBlank()
                ? "No reason provided"
                : dto.getReason().trim();

        adminAuditService.log(admin, target, "UNBAN_USER", reason);

        return toUserDto(target);
    }

    @Transactional
    public List<UserBanResponseDto> getBanHistory(Long userId) {
        findUser(userId);
        return userBanRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toBanDto)
                .toList();
    }

    private User findUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private User findAdmin(String adminEmail) {
        return userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found."));
    }

    private User findModerationTarget(Long userId, User admin) {
        User target = findUser(userId);

        if (target.getId().equals(admin.getId())) {
            throw new ConflictException("You cannot moderate your own account.");
        }

        if (target.getRole() == Role.ADMIN) {
            throw new ConflictException("Administrator accounts cannot be suspended or banned.");
        }

        return target;
    }

    private void deactivateActiveBans(User target, User admin, LocalDateTime now) {
        List<UserBan> activeBans = userBanRepo.findByUserIdAndActiveTrue(target.getId());

        for (UserBan activeBan : activeBans) {
            activeBan.setActive(false);
            activeBan.setLiftedAt(now);
            activeBan.setLiftedBy(admin);
            userBanRepo.save(activeBan);
        }
    }

    private AdminUserResponseDto toUserDto(User user) {
        return new AdminUserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isEmailVerified(),
                user.isPhoneVerified(),
                user.getCity(),
                user.getPhoto(),
                user.getRating(),
                user.getRole(),
                user.getAccountStatus() == null ? AccountStatus.ACTIVE : user.getAccountStatus(),
                user.getCreatedAt()
        );
    }

    private UserBanResponseDto toBanDto(UserBan ban) {
        User admin = ban.getAdmin();
        User liftedBy = ban.getLiftedBy();

        return new UserBanResponseDto(
                ban.getId(),
                ban.getUser().getId(),
                admin.getId(),
                displayName(admin),
                ban.getReason(),
                ban.getBanType(),
                ban.getStartDate(),
                ban.getEndDate(),
                ban.isActive(),
                ban.getCreatedAt(),
                ban.getLiftedAt(),
                liftedBy == null ? null : displayName(liftedBy)
        );
    }

    private String displayName(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();

        return name.isEmpty() ? user.getEmail() : name;
    }
}

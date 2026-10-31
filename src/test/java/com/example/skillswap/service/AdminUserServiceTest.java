package com.example.skillswap.service;

import com.example.skillswap.dto.request.BanUserRequestDto;
import com.example.skillswap.dto.request.SuspendUserRequestDto;
import com.example.skillswap.dto.request.UnbanUserRequestDto;
import com.example.skillswap.dto.response.AdminUserResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.entity.UserBan;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.BanType;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.Role;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.repository.UserBanRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock
    private UserRepo userRepo;
    @Mock
    private UserBanRepo userBanRepo;
    @Mock
    private ReportService reportService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AdminAuditService adminAuditService;

    private AdminUserService service;
    private User admin;
    private User target;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        service = new AdminUserService(
                userRepo,
                userBanRepo,
                reportService,
                notificationService,
                adminAuditService,
                clock
        );

        admin = user(1L, "Admin", Role.ADMIN);
        target = user(2L, "Target", Role.USER);

        lenient().when(userRepo.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        lenient().when(userRepo.findById(2L)).thenReturn(Optional.of(target));
        lenient().when(userBanRepo.findByUserIdAndActiveTrue(2L)).thenReturn(List.of());
    }

    private User user(Long id, String firstName, Role role) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setEmail(firstName.toLowerCase() + "@example.com");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }

    @Test
    void suspendUserCreatesTemporaryBan() {
        SuspendUserRequestDto dto = new SuspendUserRequestDto();
        dto.setReason("  spam  ");
        dto.setDurationDays(7);

        AdminUserResponseDto result = service.suspendUser(2L, dto, admin.getEmail());

        assertEquals(AccountStatus.SUSPENDED, result.getAccountStatus());
        assertEquals(AccountStatus.SUSPENDED, target.getAccountStatus());

        ArgumentCaptor<UserBan> banCaptor = ArgumentCaptor.forClass(UserBan.class);
        verify(userBanRepo).save(banCaptor.capture());
        UserBan ban = banCaptor.getValue();
        assertEquals(BanType.TEMPORARY, ban.getBanType());
        assertEquals("spam", ban.getReason());
        assertTrue(ban.isActive());
        assertEquals(LocalDateTime.of(2026, 11, 6, 12, 0), ban.getEndDate());

        verify(notificationService).createNotification(
                eq(2L),
                anyString(),
                eq(NotificationType.ACCOUNT_SUSPENDED)
        );
        verify(adminAuditService).log(eq(admin), eq(target), eq("SUSPEND_USER"), anyString());
    }

    @Test
    void banUserCreatesPermanentBan() {
        BanUserRequestDto dto = new BanUserRequestDto();
        dto.setReason("fraud");

        AdminUserResponseDto result = service.banUser(2L, dto, admin.getEmail());

        assertEquals(AccountStatus.BANNED, result.getAccountStatus());

        ArgumentCaptor<UserBan> banCaptor = ArgumentCaptor.forClass(UserBan.class);
        verify(userBanRepo).save(banCaptor.capture());
        UserBan ban = banCaptor.getValue();
        assertEquals(BanType.PERMANENT, ban.getBanType());
        assertNull(ban.getEndDate());

        verify(notificationService).createNotification(
                eq(2L),
                anyString(),
                eq(NotificationType.ACCOUNT_BANNED)
        );
    }

    @Test
    void unbanUserReactivatesAccount() {
        target.setAccountStatus(AccountStatus.SUSPENDED);

        AdminUserResponseDto result = service.unbanUser(
                2L,
                new UnbanUserRequestDto("appeal accepted"),
                admin.getEmail()
        );

        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        verify(notificationService).createNotification(
                eq(2L),
                anyString(),
                eq(NotificationType.ACCOUNT_UNBANNED)
        );
    }

    @Test
    void alreadyActiveUserCannotBeUnbanned() {
        assertThrows(
                ConflictException.class,
                () -> service.unbanUser(2L, null, admin.getEmail())
        );
    }

    @Test
    void adminCannotModerateSelf() {
        SuspendUserRequestDto dto = new SuspendUserRequestDto();
        dto.setReason("test");
        dto.setDurationDays(1);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(
                ConflictException.class,
                () -> service.suspendUser(1L, dto, admin.getEmail())
        );
    }

    @Test
    void adminsCannotBeSuspendedOrBanned() {
        User otherAdmin = user(3L, "Other", Role.ADMIN);
        when(userRepo.findById(3L)).thenReturn(Optional.of(otherAdmin));

        BanUserRequestDto dto = new BanUserRequestDto();
        dto.setReason("test");

        assertThrows(
                ConflictException.class,
                () -> service.banUser(3L, dto, admin.getEmail())
        );
    }
}

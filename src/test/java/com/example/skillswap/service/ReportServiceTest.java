package com.example.skillswap.service;

import com.example.skillswap.dto.request.ReportRequestDto;
import com.example.skillswap.dto.response.ReportResponseDto;
import com.example.skillswap.entity.Report;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.ReportReason;
import com.example.skillswap.enums.ReportStatus;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.ReportRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock
    private ReportRepo reportRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AdminAuditService adminAuditService;

    private ReportService service;
    private User reporter;
    private User reportedUser;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        service = new ReportService(
                reportRepo,
                userRepo,
                notificationService,
                adminAuditService,
                clock
        );

        reporter = new User();
        reporter.setId(1L);
        reporter.setFirstName("Reporter");
        reporter.setEmail("reporter@example.com");

        reportedUser = new User();
        reportedUser.setId(2L);
        reportedUser.setFirstName("Reported");
        reportedUser.setEmail("reported@example.com");
    }

    private ReportRequestDto dto() {
        ReportRequestDto dto = new ReportRequestDto();
        dto.setReason(ReportReason.HARASSMENT);
        dto.setDescription("  abusive messages  ");
        return dto;
    }

    @Test
    void createReport() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(reporter));
        when(userRepo.findById(2L)).thenReturn(Optional.of(reportedUser));
        when(reportRepo.existsByReporterIdAndReportedUserIdAndReasonAndStatusIn(
                1L,
                2L,
                ReportReason.HARASSMENT,
                EnumSet.of(ReportStatus.PENDING, ReportStatus.UNDER_REVIEW)
        )).thenReturn(false);
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(10L);
            return report;
        });

        ReportResponseDto result = service.createReport(1L, 2L, dto());

        assertEquals(10L, result.getId());
        assertEquals(2L, result.getReportedUserId());
        assertEquals("abusive messages", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
        verifyNoInteractions(notificationService, adminAuditService);
    }

    @Test
    void selfReportIsRejected() {
        assertThrows(ConflictException.class, () -> service.createReport(1L, 1L, dto()));
        verifyNoInteractions(userRepo, reportRepo);
    }

    @Test
    void missingReportedUserIsRejected() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(reporter));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createReport(1L, 2L, dto()));
    }

    @Test
    void duplicateActiveReportIsRejected() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(reporter));
        when(userRepo.findById(2L)).thenReturn(Optional.of(reportedUser));
        when(reportRepo.existsByReporterIdAndReportedUserIdAndReasonAndStatusIn(
                anyLong(),
                anyLong(),
                any(ReportReason.class),
                anySet()
        )).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createReport(1L, 2L, dto()));
        verify(reportRepo, never()).save(any());
    }
}

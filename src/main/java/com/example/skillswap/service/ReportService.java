package com.example.skillswap.service;

import com.example.skillswap.dto.request.ReportRequestDto;
import com.example.skillswap.dto.request.ReportStatusUpdateRequestDto;
import com.example.skillswap.dto.response.ReportResponseDto;
import com.example.skillswap.entity.Report;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.ReportReason;
import com.example.skillswap.enums.ReportStatus;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.ReportRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Set<ReportStatus> ACTIVE_STATUSES =
            EnumSet.of(ReportStatus.PENDING, ReportStatus.UNDER_REVIEW);

    private final ReportRepo reportRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final AdminAuditService adminAuditService;
    private final Clock clock;

    @Transactional
    public ReportResponseDto createReport(Long reporterId, Long reportedUserId, ReportRequestDto dto) {
        if (reporterId.equals(reportedUserId)) {
            throw new ConflictException("You cannot report yourself.");
        }

        User reporter = userRepo.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found."));
        User reportedUser = userRepo.findById(reportedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reported user not found."));

        boolean duplicate = reportRepo
                .existsByReporterIdAndReportedUserIdAndReasonAndStatusIn(
                        reporterId,
                        reportedUserId,
                        dto.getReason(),
                        ACTIVE_STATUSES
                );

        if (duplicate) {
            throw new ConflictException(
                    "You already have an active report against this user for the same reason."
            );
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReason(dto.getReason());
        report.setDescription(normalizeNullable(dto.getDescription()));
        report.setStatus(ReportStatus.PENDING);

        return toResponse(reportRepo.save(report));
    }

    @Transactional
    public Page<ReportResponseDto> getMyReports(Long reporterId, Pageable pageable) {
        return reportRepo.findByReporterId(reporterId, pageable).map(this::toResponse);
    }

    @Transactional
    public Page<ReportResponseDto> searchReports(
            ReportStatus status,
            ReportReason reason,
            Pageable pageable
    ) {
        return reportRepo.searchReports(status, reason, pageable).map(this::toResponse);
    }

    @Transactional
    public Page<ReportResponseDto> getReportsForUser(Long userId, Pageable pageable) {
        return reportRepo.findByReportedUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional
    public ReportResponseDto getReport(Long reportId) {
        return toResponse(findReport(reportId));
    }

    @Transactional
    public ReportResponseDto updateStatus(
            Long reportId,
            ReportStatusUpdateRequestDto dto,
            String adminEmail
    ) {
        Report report = findReport(reportId);
        ReportStatus next = dto.getStatus();

        if (next == ReportStatus.PENDING) {
            throw new ConflictException("A report cannot be moved back to PENDING.");
        }

        if (report.getStatus() == ReportStatus.RESOLVED
                || report.getStatus() == ReportStatus.DISMISSED) {
            throw new ConflictException("This report has already been closed.");
        }

        if (report.getStatus() == ReportStatus.UNDER_REVIEW
                && next == ReportStatus.UNDER_REVIEW) {
            throw new ConflictException("This report is already under review.");
        }

        User admin = userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found."));

        ReportStatus previous = report.getStatus();
        report.setStatus(next);
        report.setAdminNote(normalizeNullable(dto.getAdminNote()));
        report.setHandledBy(admin);

        if (next == ReportStatus.RESOLVED || next == ReportStatus.DISMISSED) {
            report.setHandledAt(LocalDateTime.now(clock));
        }

        Report saved = reportRepo.save(report);

        notificationService.createNotification(
                report.getReporter().getId(),
                "Your report status changed to " + next.name().replace('_', ' ') + ".",
                NotificationType.REPORT_STATUS_UPDATE
        );

        adminAuditService.log(
                admin,
                report.getReportedUser(),
                "REPORT_" + next.name(),
                "Report #" + report.getId() + " moved from " + previous + " to " + next
                        + (dto.getAdminNote() == null ? "" : ": " + dto.getAdminNote())
        );

        return toResponse(saved);
    }

    private Report findReport(Long reportId) {
        return reportRepo.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found."));
    }

    private ReportResponseDto toResponse(Report report) {
        User reporter = report.getReporter();
        User reportedUser = report.getReportedUser();
        User handledBy = report.getHandledBy();

        return new ReportResponseDto(
                report.getId(),
                reporter.getId(),
                displayName(reporter),
                reporter.getEmail(),
                reportedUser.getId(),
                displayName(reportedUser),
                reportedUser.getEmail(),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getAdminNote(),
                handledBy == null ? null : handledBy.getId(),
                handledBy == null ? null : displayName(handledBy),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getHandledAt()
        );
    }

    private String displayName(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();

        return name.isEmpty() ? user.getEmail() : name;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

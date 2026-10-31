package com.example.skillswap.repository;

import com.example.skillswap.entity.Report;
import com.example.skillswap.enums.ReportReason;
import com.example.skillswap.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReportRepo extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndReportedUserIdAndReasonAndStatusIn(
            Long reporterId,
            Long reportedUserId,
            ReportReason reason,
            Collection<ReportStatus> statuses
    );

    Page<Report> findByReporterId(Long reporterId, Pageable pageable);

    Page<Report> findByReportedUserId(Long reportedUserId, Pageable pageable);

    boolean existsByReporterId(Long reporterId);

    boolean existsByReportedUserId(Long reportedUserId);

    long countByStatus(ReportStatus status);

    List<Report> findTop5ByStatusOrderByCreatedAtDesc(ReportStatus status);

    @Query("""
            select r from Report r
            where (:status is null or r.status = :status)
              and (:reason is null or r.reason = :reason)
            """)
    Page<Report> searchReports(
            @Param("status") ReportStatus status,
            @Param("reason") ReportReason reason,
            Pageable pageable
    );
}

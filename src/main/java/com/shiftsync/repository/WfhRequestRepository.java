package com.shiftsync.repository;

import com.shiftsync.entity.WfhRequest;
import com.shiftsync.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WfhRequestRepository extends JpaRepository<WfhRequest, Long> {

    Page<WfhRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<WfhRequest> findByUserIdAndStatus(Long userId, RequestStatus status);

    // Check for duplicate WFH on same date
    boolean existsByUserIdAndWfhDateAndStatusNot(Long userId, LocalDate wfhDate, RequestStatus status);

    // Count approved WFH in current month
    @Query("""
            SELECT COUNT(w) FROM WfhRequest w
            WHERE w.user.id = :userId
            AND w.status = 'APPROVED'
            AND MONTH(w.wfhDate) = :month
            AND YEAR(w.wfhDate) = :year
            """)
    long countApprovedWfhInMonth(@Param("userId") Long userId,
                                 @Param("month") int month,
                                 @Param("year") int year);

    // Team WFH calendar
    @Query("""
            SELECT w FROM WfhRequest w
            WHERE w.user.teamName = :teamName
            AND w.status = 'APPROVED'
            AND w.wfhDate BETWEEN :fromDate AND :toDate
            """)
    List<WfhRequest> findTeamWfhInRange(@Param("teamName") String teamName,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate);
}
package com.shiftsync.repository;

import com.shiftsync.entity.LeaveRequest;
import com.shiftsync.enums.LeaveType;
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
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<LeaveRequest> findByUserIdAndStatus(Long userId, RequestStatus status);

    // Check for overlapping leave requests
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.id = :userId
            AND lr.status NOT IN ('REJECTED', 'CANCELLED')
            AND (lr.fromDate <= :toDate AND lr.toDate >= :fromDate)
            """)
    List<LeaveRequest> findOverlappingLeaves(@Param("userId") Long userId,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate);

    // Team calendar — all leaves in a date range
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.teamName = :teamName
            AND lr.status = 'APPROVED'
            AND (lr.fromDate <= :toDate AND lr.toDate >= :fromDate)
            """)
    List<LeaveRequest> findTeamLeavesInRange(@Param("teamName") String teamName,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate);

    List<LeaveRequest> findByUserIdAndLeaveType(Long userId, LeaveType leaveType);
}
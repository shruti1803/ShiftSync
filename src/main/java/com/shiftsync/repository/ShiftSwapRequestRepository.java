package com.shiftsync.repository;

import com.shiftsync.entity.ShiftSwapRequest;
import com.shiftsync.enums.SwapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {

    // All swaps initiated BY this user
    Page<ShiftSwapRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId, Pageable pageable);

    // All swaps where this user is the TARGET (awaiting their confirmation)
    List<ShiftSwapRequest> findByTargetIdAndStatus(Long targetId, SwapStatus status);

    // All pending swaps involving this user (either as requester or target)
    @Query("""
            SELECT s FROM ShiftSwapRequest s
            WHERE (s.requester.id = :userId OR s.target.id = :userId)
            AND s.status IN ('PENDING_TARGET_APPROVAL', 'PENDING_MANAGER_APPROVAL')
            """)
    List<ShiftSwapRequest> findPendingSwapsForUser(@Param("userId") Long userId);
}
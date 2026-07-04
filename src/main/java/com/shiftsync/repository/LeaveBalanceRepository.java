package com.shiftsync.repository;

import com.shiftsync.entity.LeaveBalance;
import com.shiftsync.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByUserIdAndLeaveType(Long userId, LeaveType leaveType);

    List<LeaveBalance> findByUserId(Long userId);
}
package com.shiftsync.service;

import com.shiftsync.dto.request.LeaveRequestDto;
import com.shiftsync.dto.response.LeaveBalanceResponse;
import com.shiftsync.dto.response.LeaveRequestResponse;
import com.shiftsync.entity.LeaveBalance;
import com.shiftsync.entity.LeaveRequest;
import com.shiftsync.entity.User;
import com.shiftsync.enums.LeaveType;
import com.shiftsync.enums.RequestStatus;
import com.shiftsync.exception.BusinessException;
import com.shiftsync.exception.ResourceNotFoundException;
import com.shiftsync.repository.CompOffCreditRepository;
import com.shiftsync.repository.HolidayRepository;
import com.shiftsync.repository.LeaveBalanceRepository;
import com.shiftsync.repository.LeaveRequestRepository;
import com.shiftsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final CompOffCreditRepository compOffCreditRepository;
    private final HolidayRepository holidayRepository;
    private final UserRepository userRepository;

    private static final Map<LeaveType, String> LEAVE_DISPLAY_NAMES = Map.of(
            LeaveType.ANNUAL, "Annual Leave",
            LeaveType.MEDICAL, "Medical Leave",
            LeaveType.MATERNITY, "Maternity Leave",
            LeaveType.PATERNITY, "Paternity Leave",
            LeaveType.COMP_OFF, "Comp-Off",
            LeaveType.LOP, "Loss of Pay"
    );

    public LeaveBalanceResponse getLeaveBalances(Long userId) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByUserId(userId);
        long activeCompOff = compOffCreditRepository.countActiveCredits(userId, LocalDate.now());

        List<LeaveBalanceResponse.LeaveBalanceItem> items = balances.stream()
                .map(b -> LeaveBalanceResponse.LeaveBalanceItem.builder()
                        .leaveType(b.getLeaveType())
                        .displayName(LEAVE_DISPLAY_NAMES.getOrDefault(b.getLeaveType(), b.getLeaveType().name()))
                        .balance(b.getBalance())
                        .totalAllocated(b.getTotalAllocated())
                        .totalUsed(b.getTotalUsed())
                        .build())
                .collect(Collectors.toList());

        return LeaveBalanceResponse.builder()
                .balances(items)
                .activeCompOffCredits(activeCompOff)
                .build();
    }

    public Page<LeaveRequestResponse> getMyLeaves(Long userId, Pageable pageable) {
        return leaveRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public LeaveRequestResponse getLeaveById(Long leaveId, Long userId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request", leaveId));

        if (!leave.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only view your own leave requests");
        }
        return toResponse(leave);
    }

    @Transactional
    public LeaveRequestResponse applyLeave(LeaveRequestDto request, Long userId) {
        validateDates(request.getFromDate(), request.getToDate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check for overlapping leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                userId, request.getFromDate(), request.getToDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessException("You already have a leave request overlapping these dates");
        }

        double numberOfDays = calculateWorkingDays(request.getFromDate(), request.getToDate());
        if (numberOfDays == 0) {
            throw new BusinessException("No working days in the selected date range");
        }

        // Handle comp-off application on a holiday
        if (request.isApplyAsCompOff()) {
            return applyCompOffLeave(request, user, numberOfDays);
        }

        // Standard leave — check balance (except LOP)
        if (request.getLeaveType() != LeaveType.LOP) {
            validateAndDeductBalance(userId, request.getLeaveType(), numberOfDays);
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(user)
                .leaveType(request.getLeaveType())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .numberOfDays(numberOfDays)
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .appliedOnHoliday(false)
                .build();

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    /**
     * When employee clicks "Apply Comp-Off" on a holiday date,
     * this records it as a comp-off leave request and marks it as applied on a holiday.
     */
    private LeaveRequestResponse applyCompOffLeave(LeaveRequestDto request, User user, double days) {
        // Verify it's actually a holiday
        boolean isHoliday = holidayRepository.existsByHolidayDateAndCountryCode(request.getFromDate(), "IN");
        if (!isHoliday) {
            throw new BusinessException("Comp-off can only be applied for holidays");
        }

        // Check active comp-off credits exist
        long activeCredits = compOffCreditRepository.countActiveCredits(user.getId(), LocalDate.now());
        if (activeCredits < days) {
            throw new BusinessException("Insufficient comp-off credits. You have " + activeCredits + " available.");
        }

        // Get holiday name
        String holidayName = holidayRepository
                .findByCountryCodeAndHolidayDateBetween("IN", request.getFromDate(), request.getFromDate())
                .stream().findFirst().map(h -> h.getName()).orElse("Holiday");

        // Deduct from COMP_OFF balance
        validateAndDeductBalance(user.getId(), LeaveType.COMP_OFF, days);

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(user)
                .leaveType(LeaveType.COMP_OFF)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .numberOfDays(days)
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .appliedOnHoliday(true)
                .holidayName(holidayName)
                .build();

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestResponse cancelLeave(Long leaveId, Long userId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request", leaveId));

        if (!leave.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only cancel your own leave requests");
        }
        if (leave.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Only PENDING leave requests can be cancelled");
        }
        if (leave.getFromDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot cancel a leave that has already started");
        }

        leave.setStatus(RequestStatus.CANCELLED);

        // Restore balance (except LOP)
        if (leave.getLeaveType() != LeaveType.LOP) {
            restoreBalance(userId, leave.getLeaveType(), leave.getNumberOfDays());
        }

        return toResponse(leaveRequestRepository.save(leave));
    }

    public List<LeaveRequestResponse> getTeamLeaves(String teamName, LocalDate from, LocalDate to) {
        return leaveRequestRepository.findTeamLeavesInRange(teamName, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void validateDates(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("From date cannot be after to date");
        }
        if (from.isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot apply leave for past dates");
        }
    }

    /**
     * Counts working days (Mon–Fri) in range, excluding weekends.
     * (For a more advanced version, also exclude public holidays.)
     */
    private double calculateWorkingDays(LocalDate from, LocalDate to) {
        long count = 0;
        LocalDate current = from;
        while (!current.isAfter(to)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private void validateAndDeductBalance(Long userId, LeaveType leaveType, double days) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveType(userId, leaveType)
                .orElseThrow(() -> new BusinessException("No balance record found for " + leaveType));

        if (balance.getBalance() < days) {
            throw new BusinessException("Insufficient " + LEAVE_DISPLAY_NAMES.get(leaveType)
                    + " balance. Available: " + balance.getBalance() + ", Requested: " + days);
        }

        balance.setBalance(balance.getBalance() - days);
        balance.setTotalUsed(balance.getTotalUsed() + days);
        leaveBalanceRepository.save(balance);
    }

    private void restoreBalance(Long userId, LeaveType leaveType, double days) {
        leaveBalanceRepository.findByUserIdAndLeaveType(userId, leaveType).ifPresent(balance -> {
            balance.setBalance(balance.getBalance() + days);
            balance.setTotalUsed(balance.getTotalUsed() - days);
            leaveBalanceRepository.save(balance);
        });
    }

    private LeaveRequestResponse toResponse(LeaveRequest leave) {
        return LeaveRequestResponse.builder()
                .id(leave.getId())
                .leaveType(leave.getLeaveType())
                .leaveTypeDisplay(LEAVE_DISPLAY_NAMES.getOrDefault(leave.getLeaveType(), leave.getLeaveType().name()))
                .fromDate(leave.getFromDate())
                .toDate(leave.getToDate())
                .numberOfDays(leave.getNumberOfDays())
                .reason(leave.getReason())
                .status(leave.getStatus())
                .managerComment(leave.getManagerComment())
                .appliedOnHoliday(leave.getAppliedOnHoliday())
                .holidayName(leave.getHolidayName())
                .appliedAt(leave.getCreatedAt())
                .build();
    }
}
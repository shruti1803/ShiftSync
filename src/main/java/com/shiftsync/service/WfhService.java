package com.shiftsync.service;

import com.shiftsync.dto.request.WfhRequestDto;
import com.shiftsync.dto.response.WfhBalanceResponse;
import com.shiftsync.dto.response.WfhRequestResponse;
import com.shiftsync.entity.User;
import com.shiftsync.entity.WfhBalance;
import com.shiftsync.entity.WfhRequest;
import com.shiftsync.enums.RequestStatus;
import com.shiftsync.exception.BusinessException;
import com.shiftsync.exception.ResourceNotFoundException;
import com.shiftsync.repository.UserRepository;
import com.shiftsync.repository.WfhBalanceRepository;
import com.shiftsync.repository.WfhRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WfhService {

    private final WfhRequestRepository wfhRequestRepository;
    private final WfhBalanceRepository wfhBalanceRepository;
    private final UserRepository userRepository;

    @Value("${app.wfh.monthly-allowance}")
    private int monthlyAllowance;

    public WfhBalanceResponse getMyBalance(Long userId) {
        WfhBalance balance = wfhBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("WFH balance not found for user: " + userId));

        return WfhBalanceResponse.builder()
                .currentMonthBalance(balance.getCurrentMonthBalance())
                .balanceMonth(balance.getBalanceMonth())
                .totalWfhUsedThisYear(balance.getTotalWfhUsedThisYear())
                .monthlyAllowance(monthlyAllowance)
                .build();
    }

    public Page<WfhRequestResponse> getMyRequests(Long userId, Pageable pageable) {
        return wfhRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public WfhRequestResponse applyWfh(WfhRequestDto request, Long userId) {
        if (request.getWfhDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot apply WFH for past dates");
        }

        // Prevent duplicate WFH on the same date
        boolean duplicate = wfhRequestRepository.existsByUserIdAndWfhDateAndStatusNot(
                userId, request.getWfhDate(), RequestStatus.CANCELLED);
        if (duplicate) {
            throw new BusinessException("You already have a WFH request for " + request.getWfhDate());
        }

        // Check monthly balance
        WfhBalance balance = wfhBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("WFH balance record not found"));

        // Auto-reset balance if it's a new month (safety net for scheduler)
        String currentMonth = YearMonth.now().toString();
        if (!currentMonth.equals(balance.getBalanceMonth())) {
            balance.setCurrentMonthBalance(monthlyAllowance);
            balance.setBalanceMonth(currentMonth);
        }

        if (balance.getCurrentMonthBalance() <= 0) {
            throw new BusinessException("No WFH days remaining for " + currentMonth
                    + ". Your monthly allowance of " + monthlyAllowance + " days has been used.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        WfhRequest wfhRequest = WfhRequest.builder()
                .user(user)
                .wfhDate(request.getWfhDate())
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        // Deduct from balance immediately on application
        balance.setCurrentMonthBalance(balance.getCurrentMonthBalance() - 1);
        balance.setTotalWfhUsedThisYear(balance.getTotalWfhUsedThisYear() + 1);
        wfhBalanceRepository.save(balance);

        return toResponse(wfhRequestRepository.save(wfhRequest));
    }

    @Transactional
    public WfhRequestResponse cancelWfh(Long wfhId, Long userId) {
        WfhRequest wfhRequest = wfhRequestRepository.findById(wfhId)
                .orElseThrow(() -> new ResourceNotFoundException("WFH request", wfhId));

        if (!wfhRequest.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only cancel your own WFH requests");
        }
        if (wfhRequest.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Only PENDING WFH requests can be cancelled");
        }
        if (wfhRequest.getWfhDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot cancel a WFH that has already occurred");
        }

        wfhRequest.setStatus(RequestStatus.CANCELLED);

        // Restore balance
        wfhBalanceRepository.findByUserId(userId).ifPresent(balance -> {
            balance.setCurrentMonthBalance(balance.getCurrentMonthBalance() + 1);
            balance.setTotalWfhUsedThisYear(Math.max(0, balance.getTotalWfhUsedThisYear() - 1));
            wfhBalanceRepository.save(balance);
        });

        return toResponse(wfhRequestRepository.save(wfhRequest));
    }

    public List<WfhRequestResponse> getTeamWfh(String teamName, LocalDate from, LocalDate to) {
        return wfhRequestRepository.findTeamWfhInRange(teamName, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private WfhRequestResponse toResponse(WfhRequest w) {
        return WfhRequestResponse.builder()
                .id(w.getId())
                .wfhDate(w.getWfhDate())
                .reason(w.getReason())
                .status(w.getStatus())
                .managerComment(w.getManagerComment())
                .appliedAt(w.getCreatedAt())
                .build();
    }
}
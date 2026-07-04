package com.shiftsync.service;

import com.shiftsync.dto.response.CompOffCreditResponse;
import com.shiftsync.repository.CompOffCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompOffService {

    private final CompOffCreditRepository compOffCreditRepository;

    public List<CompOffCreditResponse> getActiveCredits(Long userId) {
        LocalDate today = LocalDate.now();
        return compOffCreditRepository.findActiveCredits(userId, today).stream()
                .map(credit -> CompOffCreditResponse.builder()
                        .id(credit.getId())
                        .workedOnDate(credit.getWorkedOnDate())
                        .holidayName(credit.getHolidayName())
                        .expiryDate(credit.getExpiryDate())
                        .isUsed(credit.getIsUsed())
                        .redeemedViaLeaveRequestId(credit.getRedeemedViaLeaveRequestId())
                        .daysUntilExpiry(ChronoUnit.DAYS.between(today, credit.getExpiryDate()))
                        .build())
                .collect(Collectors.toList());
    }
}
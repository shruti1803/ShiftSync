package com.shiftsync.scheduler;

import com.shiftsync.entity.CompOffCredit;
import com.shiftsync.entity.Holiday;
import com.shiftsync.entity.LeaveBalance;
import com.shiftsync.entity.OnCallSchedule;
import com.shiftsync.enums.LeaveType;
import com.shiftsync.repository.CompOffCreditRepository;
import com.shiftsync.repository.HolidayRepository;
import com.shiftsync.repository.LeaveBalanceRepository;
import com.shiftsync.repository.OnCallScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompOffAutoCredit {

    private final OnCallScheduleRepository onCallScheduleRepository;
    private final HolidayRepository holidayRepository;
    private final CompOffCreditRepository compOffCreditRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Value("${app.compoff.expiry-months}")
    private int expiryMonths;

    /**
     * Runs daily at 6 AM.
     * Checks yesterday's on-call schedule — if it fell on a holiday or weekend,
     * auto-credits comp-off to those employees.
     */
    @Scheduled(cron = "0 0 6 * * *") // Every day at 6:00 AM
    @Transactional
    public void autoCreditCompOffForOnCallOnHolidays() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Checking comp-off eligibility for on-call on: {}", yesterday);

        boolean isWeekend = yesterday.getDayOfWeek() == DayOfWeek.SATURDAY
                || yesterday.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean isHoliday = holidayRepository.existsByHolidayDateAndCountryCode(yesterday, "IN");

        if (!isWeekend && !isHoliday) {
            log.debug("Date {} was a regular workday. No comp-off credits needed.", yesterday);
            return;
        }

        String reason = isHoliday ? getHolidayName(yesterday) : "Weekend On-Call (" + yesterday.getDayOfWeek() + ")";
        List<OnCallSchedule> onCallList = onCallScheduleRepository.findUnCreditedOnCallOnDate(yesterday);

        for (OnCallSchedule onCall : onCallList) {
            // Credit primary employee
            creditCompOff(onCall.getPrimaryUser().getId(), yesterday, reason);

            // Credit secondary employee if assigned
            if (onCall.getSecondaryUser() != null) {
                creditCompOff(onCall.getSecondaryUser().getId(), yesterday, reason);
            }

            onCall.setCompOffCredited(true);
            onCallScheduleRepository.save(onCall);
        }

        log.info("Comp-off auto-credit complete for {}. {} on-call records processed.", yesterday, onCallList.size());
    }

    private void creditCompOff(Long userId, LocalDate workedOnDate, String holidayName) {
        // Avoid duplicate credits
        if (compOffCreditRepository.existsByUserIdAndWorkedOnDate(userId, workedOnDate)) {
            log.warn("Comp-off already credited for user {} on {}. Skipping.", userId, workedOnDate);
            return;
        }

        // Create comp-off credit record
        CompOffCredit credit = CompOffCredit.builder()
                .workedOnDate(workedOnDate)
                .holidayName(holidayName)
                .expiryDate(workedOnDate.plusMonths(expiryMonths))
                .build();
        // Set user reference (avoids full load)
        compOffCreditRepository.save(credit);

        // Update leave balance
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByUserIdAndLeaveType(userId, LeaveType.COMP_OFF);
        balanceOpt.ifPresent(balance -> {
            balance.setBalance(balance.getBalance() + 1.0);
            balance.setTotalAllocated(balance.getTotalAllocated() + 1.0);
            leaveBalanceRepository.save(balance);
        });

        log.info("Comp-off credit granted to user {} for working on: {} ({})", userId, workedOnDate, holidayName);
    }

    private String getHolidayName(LocalDate date) {
        return holidayRepository.findByCountryCodeAndHolidayDateBetween("IN", date, date)
                .stream()
                .findFirst()
                .map(Holiday::getName)
                .orElse("Public Holiday");
    }
}
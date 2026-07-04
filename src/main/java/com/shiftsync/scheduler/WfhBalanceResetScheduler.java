package com.shiftsync.scheduler;

import com.shiftsync.entity.WfhBalance;
import com.shiftsync.repository.WfhBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WfhBalanceResetScheduler {

    private final WfhBalanceRepository wfhBalanceRepository;

    @Value("${app.wfh.monthly-allowance}")
    private int monthlyAllowance;

    /**
     * Runs at midnight on the 1st of every month.
     * Resets every employee's WFH balance to the monthly allowance.
     */
    @Scheduled(cron = "0 0 0 1 * *") // 1st of every month at 00:00
    @Transactional
    public void resetMonthlyWfhBalances() {
        String currentMonth = YearMonth.now().toString(); // e.g. "2025-08"
        List<WfhBalance> allBalances = wfhBalanceRepository.findAll();

        log.info("Running WFH monthly reset for {} employees. Month: {}", allBalances.size(), currentMonth);

        for (WfhBalance balance : allBalances) {
            balance.setCurrentMonthBalance(monthlyAllowance);
            balance.setBalanceMonth(currentMonth);
        }

        wfhBalanceRepository.saveAll(allBalances);
        log.info("WFH balance reset complete. Each employee now has {} WFH days.", monthlyAllowance);
    }
}
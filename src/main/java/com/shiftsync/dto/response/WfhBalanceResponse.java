package com.shiftsync.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WfhBalanceResponse {
    private Integer currentMonthBalance;
    private String balanceMonth;
    private Integer totalWfhUsedThisYear;
    private Integer monthlyAllowance; // Always 2
}
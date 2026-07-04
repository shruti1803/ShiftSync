package com.shiftsync.dto.response;

import com.shiftsync.enums.LeaveType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaveBalanceResponse {
    private List<LeaveBalanceItem> balances;
    private long activeCompOffCredits;

    @Data
    @Builder
    public static class LeaveBalanceItem {
        private LeaveType leaveType;
        private String displayName;
        private Double balance;
        private Double totalAllocated;
        private Double totalUsed;
    }
}
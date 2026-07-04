package com.shiftsync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CompOffCreditResponse {
    private Long id;
    private LocalDate workedOnDate;
    private String holidayName;
    private LocalDate expiryDate;
    private Boolean isUsed;
    private Long redeemedViaLeaveRequestId;

    // Days remaining before expiry
    private long daysUntilExpiry;
}
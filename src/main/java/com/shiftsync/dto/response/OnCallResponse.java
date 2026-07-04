package com.shiftsync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class OnCallResponse {
    private Long id;
    private LocalDate onCallDate;
    private UserResponse primaryUser;
    private UserResponse secondaryUser;
    private Boolean primaryAcknowledged;
    private LocalDateTime primaryAcknowledgedAt;
    private Boolean secondaryAcknowledged;
    private LocalDateTime secondaryAcknowledgedAt;
    private Boolean compOffCredited;

    // Contextual flags
    private Boolean isMyDuty;            // Is the logged-in user primary or secondary?
    private Boolean iAmPrimary;
    private Boolean iHaveAcknowledged;
    private Boolean isHoliday;
    private String holidayName;
    private Boolean isWeekend;
}
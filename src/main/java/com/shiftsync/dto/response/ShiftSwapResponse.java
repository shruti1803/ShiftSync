package com.shiftsync.dto.response;

import com.shiftsync.enums.SwapStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ShiftSwapResponse {
    private Long id;
    private UserResponse requester;
    private UserResponse target;
    private LocalDate requesterShiftDate;
    private LocalDate targetShiftDate;
    private String reason;
    private SwapStatus status;
    private String statusDescription;
    private String targetComment;
    private String managerComment;
    private LocalDateTime requestedAt;
}
package com.shiftsync.dto.request;

import com.shiftsync.enums.LeaveType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @NotBlank(message = "Reason is required")
    private String reason;

    // Optional: true if employee is applying on a holiday and wants comp-off instead
    private boolean applyAsCompOff = false;

    // Auto-populated by backend when applyAsCompOff = true
    private String holidayName;
}
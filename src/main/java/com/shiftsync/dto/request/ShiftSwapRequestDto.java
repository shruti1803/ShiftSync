package com.shiftsync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftSwapRequestDto {

    @NotNull(message = "Target employee ID is required")
    private Long targetUserId;

    @NotNull(message = "Your shift date to swap away is required")
    private LocalDate requesterShiftDate;

    @NotNull(message = "Target's shift date you want is required")
    private LocalDate targetShiftDate;

    @NotBlank(message = "Reason is required")
    private String reason;
}

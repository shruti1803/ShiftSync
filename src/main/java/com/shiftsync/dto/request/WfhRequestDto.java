package com.shiftsync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WfhRequestDto {

    @NotNull(message = "WFH date is required")
    private LocalDate wfhDate;

    @NotBlank(message = "Reason is required")
    private String reason;
}
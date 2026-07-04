package com.shiftsync.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwapRespondDto {

    @NotNull(message = "accepted field is required (true = accept, false = reject)")
    private Boolean accepted;

    private String comment;
}
package com.shiftsync.dto.response;

import com.shiftsync.enums.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WfhRequestResponse {
    private Long id;
    private LocalDate wfhDate;
    private String reason;
    private RequestStatus status;
    private String managerComment;
    private LocalDateTime appliedAt;
}
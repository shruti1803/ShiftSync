package com.shiftsync.dto.response;

import com.shiftsync.enums.LeaveType;
import com.shiftsync.enums.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LeaveRequestResponse {
    private Long id;
    private LeaveType leaveType;
    private String leaveTypeDisplay;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Double numberOfDays;
    private String reason;
    private RequestStatus status;
    private String managerComment;
    private Boolean appliedOnHoliday;
    private String holidayName;
    private LocalDateTime appliedAt;
}
package com.shiftsync.dto.response;

import com.shiftsync.enums.ShiftType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ShiftScheduleResponse {
    private Long scheduleId;
    private LocalDate shiftDate;
    private ShiftType shiftType;
    private String shiftDisplayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isSwapped;
    private Boolean isToday;

    // Holidays on this shift date (relevant for US shifts)
    private List<HolidayResponse> holidays;

    // Whether there's a US holiday today (shown for US shift employees)
    private Boolean hasUsHoliday;
    private String usHolidayName;
}
package com.shiftsync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public enum ShiftType {
    MORNING("Morning Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), false),
    AFTERNOON("Afternoon Shift", LocalTime.of(14, 0), LocalTime.of(22, 0), false),
    NIGHT("Night Shift", LocalTime.of(22, 0), LocalTime.of(6, 0), false),
    US_SHIFT("US Shift", LocalTime.of(20, 0), LocalTime.of(4, 0), true),
    GENERAL("General Shift", LocalTime.of(9, 0), LocalTime.of(18, 0), false);

    private final String displayName;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean usShift; // If true, show US holidays for this employee
}
package com.shiftsync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HolidayResponse {
    private Long id;
    private LocalDate holidayDate;
    private String name;
    private String countryCode;
    private Boolean isOptional;
    private String description;
    private Boolean isUsHoliday;
    private Boolean isIndiaHoliday;
}
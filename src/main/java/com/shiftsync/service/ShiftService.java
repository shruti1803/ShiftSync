package com.shiftsync.service;

import com.shiftsync.dto.response.HolidayResponse;
import com.shiftsync.dto.response.ShiftScheduleResponse;
import com.shiftsync.entity.Holiday;
import com.shiftsync.entity.ShiftSchedule;
import com.shiftsync.entity.User;
import com.shiftsync.exception.ResourceNotFoundException;
import com.shiftsync.repository.HolidayRepository;
import com.shiftsync.repository.ShiftScheduleRepository;
import com.shiftsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftScheduleRepository shiftScheduleRepository;
    private final HolidayRepository holidayRepository;
    private final UserRepository userRepository;

    public List<ShiftScheduleResponse> getMyShifts(Long userId, LocalDate from, LocalDate to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<ShiftSchedule> schedules = shiftScheduleRepository
                .findByUserIdAndShiftDateBetweenOrderByShiftDate(userId, from, to);

        // Fetch all holidays in range once (India + US) for efficient lookup
        List<Holiday> holidays = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(from, to);
        Map<LocalDate, List<Holiday>> holidaysByDate = holidays.stream()
                .collect(Collectors.groupingBy(Holiday::getHolidayDate));

        boolean isUsShiftEmployee = user.getDefaultShift().isUsShift();

        return schedules.stream()
                .map(schedule -> toResponse(schedule, holidaysByDate.getOrDefault(schedule.getShiftDate(), List.of()), isUsShiftEmployee))
                .collect(Collectors.toList());
    }

    public ShiftScheduleResponse getTodayShift(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        LocalDate today = LocalDate.now();
        ShiftSchedule schedule = shiftScheduleRepository.findByUserIdAndShiftDate(userId, today)
                .orElseThrow(() -> new ResourceNotFoundException("No shift scheduled for today"));

        List<Holiday> todaysHolidays = holidayRepository
                .findByHolidayDateBetweenOrderByHolidayDate(today, today);

        boolean isUsShiftEmployee = user.getDefaultShift().isUsShift();

        return toResponse(schedule, todaysHolidays, isUsShiftEmployee);
    }

    @Cacheable(value = "teamShifts", key = "#teamName + '-' + #from + '-' + #to")
    public List<ShiftScheduleResponse> getTeamShifts(String teamName, LocalDate from, LocalDate to) {
        List<ShiftSchedule> schedules = shiftScheduleRepository.findTeamShiftsInRange(teamName, from, to);
        List<Holiday> holidays = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(from, to);
        Map<LocalDate, List<Holiday>> holidaysByDate = holidays.stream()
                .collect(Collectors.groupingBy(Holiday::getHolidayDate));

        return schedules.stream()
                .map(schedule -> {
                    boolean isUsShift = schedule.getUser().getDefaultShift().isUsShift();
                    return toResponse(schedule, holidaysByDate.getOrDefault(schedule.getShiftDate(), List.of()), isUsShift);
                })
                .collect(Collectors.toList());
    }

    private ShiftScheduleResponse toResponse(ShiftSchedule schedule, List<Holiday> holidaysOnDate, boolean isUsShiftEmployee) {
        List<HolidayResponse> holidayResponses = holidaysOnDate.stream()
                .map(h -> HolidayResponse.builder()
                        .id(h.getId())
                        .holidayDate(h.getHolidayDate())
                        .name(h.getName())
                        .countryCode(h.getCountryCode())
                        .isOptional(h.getIsOptional())
                        .description(h.getDescription())
                        .isUsHoliday("US".equals(h.getCountryCode()))
                        .isIndiaHoliday("IN".equals(h.getCountryCode()))
                        .build())
                .collect(Collectors.toList());

        // Only flag US holiday banner if employee is on a US shift
        boolean hasUsHoliday = false;
        String usHolidayName = null;
        if (isUsShiftEmployee) {
            var usHoliday = holidaysOnDate.stream()
                    .filter(h -> "US".equals(h.getCountryCode()))
                    .findFirst();
            if (usHoliday.isPresent()) {
                hasUsHoliday = true;
                usHolidayName = usHoliday.get().getName();
            }
        }

        return ShiftScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .shiftDate(schedule.getShiftDate())
                .shiftType(schedule.getShiftType())
                .shiftDisplayName(schedule.getShiftType().getDisplayName())
                .startTime(schedule.getShiftType().getStartTime())
                .endTime(schedule.getShiftType().getEndTime())
                .isSwapped(schedule.getIsSwapped())
                .isToday(schedule.getShiftDate().isEqual(LocalDate.now()))
                .holidays(holidayResponses)
                .hasUsHoliday(hasUsHoliday)
                .usHolidayName(usHolidayName)
                .build();
    }
}
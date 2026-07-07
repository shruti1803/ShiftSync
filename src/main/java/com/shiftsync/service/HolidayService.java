package com.shiftsync.service;

import com.shiftsync.dto.response.HolidayResponse;
import com.shiftsync.entity.Holiday;
import com.shiftsync.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    public List<HolidayResponse> getAllHolidays(LocalDate from, LocalDate to) {
        return holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<HolidayResponse> getIndiaHolidays(LocalDate from, LocalDate to) {
        return holidayRepository.findByCountryCodeAndHolidayDateBetween("IN", from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<HolidayResponse> getUsHolidays(LocalDate from, LocalDate to) {
        return holidayRepository.findByCountryCodeAndHolidayDateBetween("US", from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<HolidayResponse> getTodayHolidays() {
        LocalDate today = LocalDate.now();
        return holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(today, today)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private HolidayResponse toResponse(Holiday h) {
        return HolidayResponse.builder()
                .id(h.getId())
                .holidayDate(h.getHolidayDate())
                .name(h.getName())
                .countryCode(h.getCountryCode())
                .isOptional(h.getIsOptional())
                .description(h.getDescription())
                .isUsHoliday("US".equals(h.getCountryCode()))
                .isIndiaHoliday("IN".equals(h.getCountryCode()))
                .build();
    }
}
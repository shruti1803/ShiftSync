package com.shiftsync.controller;

import com.shiftsync.dto.response.HolidayResponse;
import com.shiftsync.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Tag(name = "Holidays", description = "India and US holiday calendars")
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @Operation(summary = "Get all holidays (India + US) in a date range")
    public ResponseEntity<List<HolidayResponse>> getAllHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(holidayService.getAllHolidays(from, to));
    }

    @GetMapping("/india")
    @Operation(summary = "Get India public holidays in a date range")
    public ResponseEntity<List<HolidayResponse>> getIndiaHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(holidayService.getIndiaHolidays(from, to));
    }

    @GetMapping("/us")
    @Operation(summary = "Get US federal holidays in a date range — relevant for US-shift employees")
    public ResponseEntity<List<HolidayResponse>> getUsHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(holidayService.getUsHolidays(from, to));
    }

    @GetMapping("/today")
    @Operation(summary = "Check if today is a holiday (India or US)")
    public ResponseEntity<List<HolidayResponse>> getTodayHolidays() {
        return ResponseEntity.ok(holidayService.getTodayHolidays());
    }
}
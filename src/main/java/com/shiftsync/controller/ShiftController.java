package com.shiftsync.controller;

import com.shiftsync.dto.response.ShiftScheduleResponse;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "Shift Calendar", description = "View shift schedules with holiday overlays")
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping("/my")
    @Operation(summary = "Get my shift schedule for a date range, with holiday overlay")
    public ResponseEntity<List<ShiftScheduleResponse>> getMyShifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftService.getMyShifts(userId, from, to));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's shift details, including any holiday flag")
    public ResponseEntity<ShiftScheduleResponse> getTodayShift() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftService.getTodayShift(userId));
    }

    @GetMapping("/team")
    @Operation(summary = "View team shift calendar — see who has which shift today")
    public ResponseEntity<List<ShiftScheduleResponse>> getTeamShifts(
            @RequestParam String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(shiftService.getTeamShifts(teamName, from, to));
    }
}
package com.shiftsync.controller;

import com.shiftsync.dto.request.LeaveRequestDto;
import com.shiftsync.dto.response.LeaveBalanceResponse;
import com.shiftsync.dto.response.LeaveRequestResponse;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Apply for leave, view balances, and track requests")
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping("/balances")
    @Operation(summary = "Get my leave balances across all leave types")
    public ResponseEntity<LeaveBalanceResponse> getMyBalances() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(leaveService.getLeaveBalances(userId));
    }

    @GetMapping
    @Operation(summary = "Get my leave request history (paginated)")
    public ResponseEntity<Page<LeaveRequestResponse>> getMyLeaves(Pageable pageable) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(leaveService.getMyLeaves(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific leave request by ID")
    public ResponseEntity<LeaveRequestResponse> getLeaveById(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(leaveService.getLeaveById(id, userId));
    }

    @PostMapping
    @Operation(summary = "Apply for leave. Set applyAsCompOff=true when applying for a holiday using comp-off credits.")
    public ResponseEntity<LeaveRequestResponse> applyLeave(@Valid @RequestBody LeaveRequestDto request) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(leaveService.applyLeave(request, userId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending leave request")
    public ResponseEntity<LeaveRequestResponse> cancelLeave(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(leaveService.cancelLeave(id, userId));
    }

    @GetMapping("/team")
    @Operation(summary = "View team leave calendar for a date range")
    public ResponseEntity<List<LeaveRequestResponse>> getTeamLeaves(
            @RequestParam String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(leaveService.getTeamLeaves(teamName, from, to));
    }
}
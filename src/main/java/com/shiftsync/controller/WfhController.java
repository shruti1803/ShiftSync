package com.shiftsync.controller;

import com.shiftsync.dto.request.WfhRequestDto;
import com.shiftsync.dto.response.WfhBalanceResponse;
import com.shiftsync.dto.response.WfhRequestResponse;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.WfhService;
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
@RequestMapping("/api/v1/wfh")
@RequiredArgsConstructor
@Tag(name = "Work From Home", description = "Apply for WFH and track monthly balance")
public class WfhController {

    private final WfhService wfhService;

    @GetMapping("/balance")
    @Operation(summary = "Get my current month's WFH balance (resets monthly)")
    public ResponseEntity<WfhBalanceResponse> getMyBalance() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(wfhService.getMyBalance(userId));
    }

    @GetMapping
    @Operation(summary = "Get my WFH request history (paginated)")
    public ResponseEntity<Page<WfhRequestResponse>> getMyRequests(Pageable pageable) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(wfhService.getMyRequests(userId, pageable));
    }

    @PostMapping
    @Operation(summary = "Apply for WFH on a specific date")
    public ResponseEntity<WfhRequestResponse> applyWfh(@Valid @RequestBody WfhRequestDto request) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(wfhService.applyWfh(request, userId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending WFH request")
    public ResponseEntity<WfhRequestResponse> cancelWfh(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(wfhService.cancelWfh(id, userId));
    }

    @GetMapping("/team")
    @Operation(summary = "View team WFH calendar for a date range")
    public ResponseEntity<List<WfhRequestResponse>> getTeamWfh(
            @RequestParam String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(wfhService.getTeamWfh(teamName, from, to));
    }
}
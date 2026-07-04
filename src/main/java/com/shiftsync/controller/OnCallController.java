package com.shiftsync.controller;

import com.shiftsync.dto.response.OnCallResponse;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.OnCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/on-call")
@RequiredArgsConstructor
@Tag(name = "On-Call", description = "On-call roster and acknowledgement (DevOps/infra niche feature)")
public class OnCallController {

    private final OnCallService onCallService;

    @GetMapping("/upcoming")
    @Operation(summary = "Get my upcoming on-call duties")
    public ResponseEntity<List<OnCallResponse>> getUpcoming() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(onCallService.getUpcomingForUser(userId));
    }

    @GetMapping("/roster")
    @Operation(summary = "Get the full on-call roster for a date range")
    public ResponseEntity<List<OnCallResponse>> getRoster(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(onCallService.getRoster(from, to, userId));
    }

    @PatchMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge that I am on-call for this duty")
    public ResponseEntity<OnCallResponse> acknowledge(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(onCallService.acknowledge(id, userId));
    }
}
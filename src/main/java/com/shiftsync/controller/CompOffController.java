package com.shiftsync.controller;

import com.shiftsync.dto.response.CompOffCreditResponse;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.CompOffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comp-off")
@RequiredArgsConstructor
@Tag(name = "Comp-Off", description = "View comp-off credits (auto-credited for on-call work on holidays/weekends)")
public class CompOffController {

    private final CompOffService compOffService;

    @GetMapping("/credits")
    @Operation(summary = "Get my active (unused, not expired) comp-off credits")
    public ResponseEntity<List<CompOffCreditResponse>> getActiveCredits() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(compOffService.getActiveCredits(userId));
    }
}
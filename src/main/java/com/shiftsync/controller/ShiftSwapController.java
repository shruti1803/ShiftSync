package com.shiftsync.controller;

import com.shiftsync.dto.request.ShiftSwapRequestDto;
import com.shiftsync.dto.request.SwapRespondDto;
import com.shiftsync.dto.response.ShiftSwapResponse;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.ShiftSwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shift-swaps")
@RequiredArgsConstructor
@Tag(name = "Shift Swap", description = "Request and manage shift swaps with teammates (mock approval workflow)")
public class ShiftSwapController {

    private final ShiftSwapService shiftSwapService;

    @GetMapping
    @Operation(summary = "Get my swap requests (the ones I initiated), paginated")
    public ResponseEntity<Page<ShiftSwapResponse>> getMySwaps(Pageable pageable) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftSwapService.getMySwaps(userId, pageable));
    }

    @GetMapping("/incoming")
    @Operation(summary = "Get swap requests waiting for MY confirmation (I'm the target)")
    public ResponseEntity<List<ShiftSwapResponse>> getIncomingSwaps() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftSwapService.getIncomingSwaps(userId));
    }

    @PostMapping
    @Operation(summary = "Request a shift swap with a teammate")
    public ResponseEntity<ShiftSwapResponse> requestSwap(@Valid @RequestBody ShiftSwapRequestDto request) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftSwapService.requestSwap(request, userId));
    }

    @PatchMapping("/{id}/respond")
    @Operation(summary = "Target employee accepts or rejects the swap request (mock approval step)")
    public ResponseEntity<ShiftSwapResponse> respondToSwap(
            @PathVariable Long id, @Valid @RequestBody SwapRespondDto response) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftSwapService.respondToSwap(id, response, userId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a swap request (only before it's approved)")
    public ResponseEntity<ShiftSwapResponse> cancelSwap(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(shiftSwapService.cancelSwap(id, userId));
    }
}
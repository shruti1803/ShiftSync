package com.shiftsync.service;

import com.shiftsync.dto.request.ShiftSwapRequestDto;
import com.shiftsync.dto.request.SwapRespondDto;
import com.shiftsync.dto.response.ShiftSwapResponse;
import com.shiftsync.dto.response.UserResponse;
import com.shiftsync.entity.ShiftSchedule;
import com.shiftsync.entity.ShiftSwapRequest;
import com.shiftsync.entity.User;
import com.shiftsync.enums.SwapStatus;
import com.shiftsync.exception.BusinessException;
import com.shiftsync.exception.ResourceNotFoundException;
import com.shiftsync.repository.ShiftScheduleRepository;
import com.shiftsync.repository.ShiftSwapRequestRepository;
import com.shiftsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftSwapService {

    private final ShiftSwapRequestRepository swapRequestRepository;
    private final ShiftScheduleRepository shiftScheduleRepository;
    private final UserRepository userRepository;

    private static final Map<SwapStatus, String> STATUS_DESCRIPTIONS = Map.of(
            SwapStatus.PENDING_TARGET_APPROVAL, "Waiting for the other employee to confirm",
            SwapStatus.PENDING_MANAGER_APPROVAL, "Both confirmed — waiting for manager approval",
            SwapStatus.APPROVED, "Swap approved and applied",
            SwapStatus.REJECTED, "Swap was rejected",
            SwapStatus.CANCELLED, "Swap was cancelled"
    );

    @Transactional
    public ShiftSwapResponse requestSwap(ShiftSwapRequestDto request, Long requesterId) {
        if (requesterId.equals(request.getTargetUserId())) {
            throw new BusinessException("You cannot request a swap with yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requesterId));
        User target = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user", request.getTargetUserId()));

        // Verify requester has a shift on the date they want to give away
        ShiftSchedule requesterShift = shiftScheduleRepository
                .findByUserIdAndShiftDate(requesterId, request.getRequesterShiftDate())
                .orElseThrow(() -> new BusinessException("You don't have a shift scheduled on " + request.getRequesterShiftDate()));

        // Verify target has a shift on the date the requester wants
        shiftScheduleRepository.findByUserIdAndShiftDate(request.getTargetUserId(), request.getTargetShiftDate())
                .orElseThrow(() -> new BusinessException(target.getName() + " doesn't have a shift scheduled on " + request.getTargetShiftDate()));

        ShiftSwapRequest swap = ShiftSwapRequest.builder()
                .requester(requester)
                .target(target)
                .requesterShiftDate(request.getRequesterShiftDate())
                .targetShiftDate(request.getTargetShiftDate())
                .reason(request.getReason())
                .status(SwapStatus.PENDING_TARGET_APPROVAL)
                .build();

        return toResponse(swapRequestRepository.save(swap));
    }

    /**
     * Target employee responds to the swap request (mock approval step #1).
     * If accepted, moves to PENDING_MANAGER_APPROVAL (mock approval step #2, auto-approved here for simplicity).
     */
    @Transactional
    public ShiftSwapResponse respondToSwap(Long swapId, SwapRespondDto response, Long targetUserId) {
        ShiftSwapRequest swap = swapRequestRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request", swapId));

        if (!swap.getTarget().getId().equals(targetUserId)) {
            throw new BusinessException("You are not authorized to respond to this swap request");
        }
        if (swap.getStatus() != SwapStatus.PENDING_TARGET_APPROVAL) {
            throw new BusinessException("This swap request is no longer pending your approval");
        }

        swap.setTargetComment(response.getComment());

        if (Boolean.TRUE.equals(response.getAccepted())) {
            // Mock manager auto-approval — in a real system this would notify a manager
            swap.setStatus(SwapStatus.APPROVED);
            swap.setManagerComment("Auto-approved (mock manager step)");
            applySwap(swap);
            log.info("Shift swap {} approved and applied", swapId);
        } else {
            swap.setStatus(SwapStatus.REJECTED);
            log.info("Shift swap {} rejected by target employee", swapId);
        }

        return toResponse(swapRequestRepository.save(swap));
    }

    @Transactional
    public ShiftSwapResponse cancelSwap(Long swapId, Long userId) {
        ShiftSwapRequest swap = swapRequestRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request", swapId));

        boolean isParty = swap.getRequester().getId().equals(userId) || swap.getTarget().getId().equals(userId);
        if (!isParty) {
            throw new BusinessException("You are not part of this swap request");
        }
        if (swap.getStatus() == SwapStatus.APPROVED) {
            throw new BusinessException("Cannot cancel an already-approved swap");
        }

        swap.setStatus(SwapStatus.CANCELLED);
        return toResponse(swapRequestRepository.save(swap));
    }

    public Page<ShiftSwapResponse> getMySwaps(Long userId, Pageable pageable) {
        return swapRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public List<ShiftSwapResponse> getIncomingSwaps(Long userId) {
        return swapRequestRepository.findByTargetIdAndStatus(userId, SwapStatus.PENDING_TARGET_APPROVAL)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Actually swaps the shift_date <-> user assignment in shift_schedules.
     */
    private void applySwap(ShiftSwapRequest swap) {
        ShiftSchedule requesterShift = shiftScheduleRepository
                .findByUserIdAndShiftDate(swap.getRequester().getId(), swap.getRequesterShiftDate())
                .orElseThrow(() -> new BusinessException("Requester's shift no longer exists"));

        ShiftSchedule targetShift = shiftScheduleRepository
                .findByUserIdAndShiftDate(swap.getTarget().getId(), swap.getTargetShiftDate())
                .orElseThrow(() -> new BusinessException("Target's shift no longer exists"));

        // Swap the shift types between the two dates/users
        var requesterShiftType = requesterShift.getShiftType();
        var targetShiftType = targetShift.getShiftType();

        requesterShift.setShiftType(targetShiftType);
        requesterShift.setIsSwapped(true);

        targetShift.setShiftType(requesterShiftType);
        targetShift.setIsSwapped(true);

        shiftScheduleRepository.save(requesterShift);
        shiftScheduleRepository.save(targetShift);
    }

    private ShiftSwapResponse toResponse(ShiftSwapRequest swap) {
        return ShiftSwapResponse.builder()
                .id(swap.getId())
                .requester(toUserResponse(swap.getRequester()))
                .target(toUserResponse(swap.getTarget()))
                .requesterShiftDate(swap.getRequesterShiftDate())
                .targetShiftDate(swap.getTargetShiftDate())
                .reason(swap.getReason())
                .status(swap.getStatus())
                .statusDescription(STATUS_DESCRIPTIONS.get(swap.getStatus()))
                .targetComment(swap.getTargetComment())
                .managerComment(swap.getManagerComment())
                .requestedAt(swap.getCreatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .defaultShift(user.getDefaultShift())
                .teamName(user.getTeamName())
                .build();
    }
}
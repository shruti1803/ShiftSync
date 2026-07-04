package com.shiftsync.service;

import com.shiftsync.dto.response.OnCallResponse;
import com.shiftsync.dto.response.UserResponse;
import com.shiftsync.entity.Holiday;
import com.shiftsync.entity.OnCallSchedule;
import com.shiftsync.entity.User;
import com.shiftsync.exception.BusinessException;
import com.shiftsync.exception.ResourceNotFoundException;
import com.shiftsync.repository.HolidayRepository;
import com.shiftsync.repository.OnCallScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnCallService {

    private final OnCallScheduleRepository onCallScheduleRepository;
    private final HolidayRepository holidayRepository;

    public List<OnCallResponse> getUpcomingForUser(Long userId) {
        return onCallScheduleRepository.findUpcomingOnCallForUser(userId, LocalDate.now())
                .stream().map(oc -> toResponse(oc, userId)).collect(Collectors.toList());
    }

    public List<OnCallResponse> getRoster(LocalDate from, LocalDate to, Long requestingUserId) {
        return onCallScheduleRepository.findByOnCallDateBetweenOrderByOnCallDate(from, to)
                .stream().map(oc -> toResponse(oc, requestingUserId)).collect(Collectors.toList());
    }

    @Transactional
    public OnCallResponse acknowledge(Long onCallId, Long userId) {
        OnCallSchedule onCall = onCallScheduleRepository.findById(onCallId)
                .orElseThrow(() -> new ResourceNotFoundException("On-call schedule", onCallId));

        boolean isPrimary = onCall.getPrimaryUser().getId().equals(userId);
        boolean isSecondary = onCall.getSecondaryUser() != null && onCall.getSecondaryUser().getId().equals(userId);

        if (!isPrimary && !isSecondary) {
            throw new BusinessException("You are not assigned to this on-call duty");
        }

        if (isPrimary) {
            onCall.setPrimaryAcknowledged(true);
            onCall.setPrimaryAcknowledgedAt(LocalDateTime.now());
        } else {
            onCall.setSecondaryAcknowledged(true);
            onCall.setSecondaryAcknowledgedAt(LocalDateTime.now());
        }

        return toResponse(onCallScheduleRepository.save(onCall), userId);
    }

    private OnCallResponse toResponse(OnCallSchedule oc, Long requestingUserId) {
        boolean iAmPrimary = oc.getPrimaryUser().getId().equals(requestingUserId);
        boolean iAmSecondary = oc.getSecondaryUser() != null && oc.getSecondaryUser().getId().equals(requestingUserId);
        boolean isMyDuty = iAmPrimary || iAmSecondary;
        boolean iHaveAcknowledged = iAmPrimary ? Boolean.TRUE.equals(oc.getPrimaryAcknowledged())
                : iAmSecondary && Boolean.TRUE.equals(oc.getSecondaryAcknowledged());

        boolean isWeekend = oc.getOnCallDate().getDayOfWeek() == DayOfWeek.SATURDAY
                || oc.getOnCallDate().getDayOfWeek() == DayOfWeek.SUNDAY;

        Optional<Holiday> holiday = holidayRepository
                .findByCountryCodeAndHolidayDateBetween("IN", oc.getOnCallDate(), oc.getOnCallDate())
                .stream().findFirst();

        return OnCallResponse.builder()
                .id(oc.getId())
                .onCallDate(oc.getOnCallDate())
                .primaryUser(toUserResponse(oc.getPrimaryUser()))
                .secondaryUser(oc.getSecondaryUser() != null ? toUserResponse(oc.getSecondaryUser()) : null)
                .primaryAcknowledged(oc.getPrimaryAcknowledged())
                .primaryAcknowledgedAt(oc.getPrimaryAcknowledgedAt())
                .secondaryAcknowledged(oc.getSecondaryAcknowledged())
                .secondaryAcknowledgedAt(oc.getSecondaryAcknowledgedAt())
                .compOffCredited(oc.getCompOffCredited())
                .isMyDuty(isMyDuty)
                .iAmPrimary(iAmPrimary)
                .iHaveAcknowledged(iHaveAcknowledged)
                .isHoliday(holiday.isPresent())
                .holidayName(holiday.map(Holiday::getName).orElse(null))
                .isWeekend(isWeekend)
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .teamName(user.getTeamName())
                .build();
    }
}
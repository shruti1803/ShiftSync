package com.shiftsync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "on_call_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnCallSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_user_id", nullable = false)
    private User primaryUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_user_id")
    private User secondaryUser;

    @Column(name = "on_call_date", nullable = false)
    private LocalDate onCallDate;

    // Employee acknowledges they are on-call
    @Column(name = "primary_acknowledged")
    @Builder.Default
    private Boolean primaryAcknowledged = false;

    @Column(name = "primary_acknowledged_at")
    private LocalDateTime primaryAcknowledgedAt;

    @Column(name = "secondary_acknowledged")
    @Builder.Default
    private Boolean secondaryAcknowledged = false;

    @Column(name = "secondary_acknowledged_at")
    private LocalDateTime secondaryAcknowledgedAt;

    // If on-call falls on weekend/holiday, comp-off is auto-credited
    @Column(name = "comp_off_credited")
    @Builder.Default
    private Boolean compOffCredited = false;
}
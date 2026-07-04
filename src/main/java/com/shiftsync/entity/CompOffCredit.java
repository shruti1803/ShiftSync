package com.shiftsync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "comp_off_credits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompOffCredit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The holiday they worked on
    @Column(name = "worked_on_date", nullable = false)
    private LocalDate workedOnDate;

    @Column(name = "holiday_name", nullable = false)
    private String holidayName;

    // Credit expires after 3 months (configurable)
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    // Links to the leave request when this comp-off is redeemed
    @Column(name = "redeemed_via_leave_request_id")
    private Long redeemedViaLeaveRequestId;
}
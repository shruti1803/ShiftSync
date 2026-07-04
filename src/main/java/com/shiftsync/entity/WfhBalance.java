package com.shiftsync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;

@Entity
@Table(name = "wfh_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WfhBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Current month's remaining WFH days
    @Column(name = "current_month_balance", nullable = false)
    @Builder.Default
    private Integer currentMonthBalance = 2;

    // Track which month this balance is for
    @Column(name = "balance_month", nullable = false)
    private String balanceMonth; // Format: "2025-01"

    // Lifetime stats
    @Column(name = "total_wfh_used_this_year", nullable = false)
    @Builder.Default
    private Integer totalWfhUsedThisYear = 0;
}
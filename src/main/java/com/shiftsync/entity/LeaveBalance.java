package com.shiftsync.entity;

import com.shiftsync.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "leave_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    @Builder.Default
    private Double balance = 0.0;

    @Column(name = "total_allocated", nullable = false)
    @Builder.Default
    private Double totalAllocated = 0.0;

    @Column(name = "total_used", nullable = false)
    @Builder.Default
    private Double totalUsed = 0.0;
}
package com.shiftsync.entity;

import com.shiftsync.enums.SwapStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "shift_swap_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftSwapRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The employee who initiates the swap
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The employee being asked to swap
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    // Requester wants to give away THIS date
    @Column(name = "requester_shift_date", nullable = false)
    private LocalDate requesterShiftDate;

    // And take target's shift on THIS date
    @Column(name = "target_shift_date", nullable = false)
    private LocalDate targetShiftDate;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SwapStatus status = SwapStatus.PENDING_TARGET_APPROVAL;

    // Target employee's response
    @Column(name = "target_comment", length = 500)
    private String targetComment;

    // Manager's response
    @Column(name = "manager_comment", length = 500)
    private String managerComment;
}
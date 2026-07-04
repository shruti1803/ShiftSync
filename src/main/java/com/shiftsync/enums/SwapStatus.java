package com.shiftsync.enums;

public enum SwapStatus {
    PENDING_TARGET_APPROVAL,  // Waiting for the other employee to confirm
    PENDING_MANAGER_APPROVAL, // Both employees agreed, now needs manager sign-off
    APPROVED,
    REJECTED,
    CANCELLED
}
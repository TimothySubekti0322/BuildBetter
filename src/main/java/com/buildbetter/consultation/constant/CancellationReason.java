package com.buildbetter.consultation.constant;

import com.buildbetter.shared.exception.BadRequestException;

import lombok.Getter;

@Getter
public enum CancellationReason {
    ARCHITECT_UNAVAILABLE("architect is unavailable"),
    INVALID_PAYMENT("proof of payment is invalid"),
    USER_CANCELLED("user cancelled the consultation"),
    SYSTEM_CANCELLED("consultation was automatically cancelled by the system");

    private final String reason;

    CancellationReason(String reason) {
        this.reason = reason;
    }

    public static CancellationReason fromString(String reason) {
        for (CancellationReason cancellationReason : CancellationReason.values()) {
            if (cancellationReason.getReason().equalsIgnoreCase(reason)) {
                return cancellationReason;
            }
        }
        throw new BadRequestException("Unknown cancellation reason: " + reason);
    }
}

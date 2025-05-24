package com.buildbetter.consultation.constant;

import lombok.Getter;

@Getter
public enum CancellationReason {
    ARCHITECT_UNAVAILABLE("architect is unavailable"),
    INVALID_PAYMENT("proof of payment is invalid");

    private final String reason;

    CancellationReason(String reason) {
        this.reason = reason;
    }
}

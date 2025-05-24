package com.buildbetter.consultation.constant;

import lombok.Getter;

@Getter
public enum ConsultationStatus {

    WAITING_FOR_PAYMENT("waiting-for-payment"),
    WAITING_FOR_CONFIRMATION("waiting-for-confirmation"),
    CANCELLED("cancelled"),
    SCHEDULED("scheduled"),
    IN_PROGRESS("in-progress"),
    ENDED("ended");

    private final String status;

    ConsultationStatus(String status) {
        this.status = status;
    }
}

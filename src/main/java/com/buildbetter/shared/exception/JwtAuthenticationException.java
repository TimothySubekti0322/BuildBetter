package com.buildbetter.shared.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    public enum Reason {
        EXPIRED, INVALID
    }

    private final Reason reason;

    public JwtAuthenticationException(String reason, Throwable cause) {
        super(reason, cause);
        this.reason = Reason.valueOf(reason);
    }

    public Reason getReason() {
        return reason;
    }
}

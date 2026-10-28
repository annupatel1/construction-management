package com.construction.construction_management.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {
    int status;
    String message;
    LocalDateTime timestamp;
    /** Optional machine-readable code the frontend can react to, e.g. PHONE_NOT_VERIFIED. */
    String code;
    /** Correlation id echoed in the X-Request-Id response header and server logs. */
    String requestId;

    public ErrorResponse(int status, String message, LocalDateTime timestamp, String code) {
        this(status, message, timestamp, code, null);
    }

    public ErrorResponse(int status, String message, LocalDateTime timestamp, String code, String requestId) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.code = code;
        this.requestId = requestId;
    }
}

package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    int status;
    String message;
    LocalDateTime timestamp;
    /** Optional machine-readable code the frontend can react to, e.g. PHONE_NOT_VERIFIED. */
    String code;
}

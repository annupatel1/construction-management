package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OtpSentResponse {
    String message;
    /** e.g. 98******10 - where the code was (or would be) sent. */
    String sentTo;
    int expiresInSeconds;
    int resendAfterSeconds;
}

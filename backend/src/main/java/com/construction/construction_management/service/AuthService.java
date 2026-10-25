package com.construction.construction_management.service;

import com.construction.construction_management.dto.*;

public interface AuthService {

    // --- registration (phone is verified by OTP before the account becomes active)
    OtpSentResponse register(RegisterRequest request);
    AuthResponse verifyRegistration(VerifyOtpRequest request);
    OtpSentResponse resendRegistrationOtp(OtpRequest request);

    // --- password login: identifier is phone OR email
    AuthResponse login(LoginRequest request);

    // --- login with a one-time code instead of a password
    OtpSentResponse requestLoginOtp(OtpRequest request);
    AuthResponse verifyLoginOtp(VerifyOtpRequest request);

    // --- forgot password
    OtpSentResponse forgotPassword(OtpRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);

    // --- logged-in user: add / change / verify email
    OtpSentResponse changeEmail(EmailRequest request);
    UserResponse verifyEmail(OtpOnlyRequest request);

    // --- revoke all tokens issued to the current account
    MessageResponse logout();
}

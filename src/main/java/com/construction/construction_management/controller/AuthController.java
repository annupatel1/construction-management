package com.construction.construction_management.controller;

import com.construction.construction_management.dto.*;
import com.construction.construction_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Public endpoints (no token needed). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public OtpSentResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyRegistration(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyRegistration(request);
    }

    @PostMapping("/resend-otp")
    public OtpSentResponse resendOtp(@Valid @RequestBody OtpRequest request) {
        return authService.resendRegistrationOtp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/login-otp/request")
    public OtpSentResponse requestLoginOtp(@Valid @RequestBody OtpRequest request) {
        return authService.requestLoginOtp(request);
    }

    @PostMapping("/login-otp/verify")
    public AuthResponse verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyLoginOtp(request);
    }

    @PostMapping("/forgot-password")
    public OtpSentResponse forgotPassword(@Valid @RequestBody OtpRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}

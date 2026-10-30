package com.construction.construction_management.controller;

import com.construction.construction_management.dto.EmailRequest;
import com.construction.construction_management.dto.OtpOnlyRequest;
import com.construction.construction_management.dto.OtpSentResponse;
import com.construction.construction_management.dto.UserResponse;
import com.construction.construction_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Logged-in user managing their own contact details. */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    /** Add or change the email; a code is sent to the new address. */
    @PostMapping("/email")
    public OtpSentResponse changeEmail(@Valid @RequestBody EmailRequest request) {
        return authService.changeEmail(request);
    }

    @PostMapping("/email/verify")
    public UserResponse verifyEmail(@Valid @RequestBody OtpOnlyRequest request) {
        return authService.verifyEmail(request);
    }
}

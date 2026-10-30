package com.construction.construction_management.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fail fast instead of starting production with development defaults. The properties file already
 * requires the environment variables; this adds checks for weak or unsafe values.
 */
@Component
@Profile("prod")
public class ProductionConfigCheck {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${otp.secret}")
    private String otpSecret;

    @Value("${otp.delivery.phone}")
    private String phoneDelivery;

    @Value("${otp.delivery.email}")
    private String emailDelivery;

    @PostConstruct
    void check() {
        requireStrongSecret("JWT_SECRET", jwtSecret);
        requireStrongSecret("OTP_SECRET", otpSecret);
        if ("CONSOLE".equalsIgnoreCase(phoneDelivery)) {
            throw new IllegalStateException("OTP_PHONE_DELIVERY must not be CONSOLE in production");
        }
        if ("CONSOLE".equalsIgnoreCase(emailDelivery)) {
            throw new IllegalStateException("OTP_EMAIL_DELIVERY must not be CONSOLE in production");
        }
    }

    private void requireStrongSecret(String name, String value) {
        if (value == null || value.length() < 32 || value.contains("dev-only")) {
            throw new IllegalStateException(name + " must be at least 32 characters and must not be a development default");
        }
    }
}

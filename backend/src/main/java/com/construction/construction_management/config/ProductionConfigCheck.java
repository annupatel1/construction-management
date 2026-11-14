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

    @Value("${otp.sms.msg91.auth-key:}")
    private String msg91AuthKey;

    @Value("${otp.sms.msg91.template-id:}")
    private String msg91TemplateId;

    @Value("${otp.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${app.cors.allowed-origins:}")
    private String corsOrigins;

    @PostConstruct
    void check() {
        requireStrongSecret("JWT_SECRET", jwtSecret);
        requireStrongSecret("OTP_SECRET", otpSecret);
        if (otpSecret.equals(jwtSecret)) {
            throw new IllegalStateException("OTP_SECRET must be different from JWT_SECRET");
        }

        requireValue("DB_URL", dbUrl);
        requireValue("DB_USERNAME", dbUsername);
        requireValue("DB_PASSWORD", dbPassword);
        requireValue("CORS_ORIGINS", corsOrigins);

        // Email OTP is the free, required channel in production.
        if (!"EMAIL".equalsIgnoreCase(emailDelivery)) {
            throw new IllegalStateException("OTP_EMAIL_DELIVERY must be EMAIL in production");
        }
        requireValue("MAIL_FROM", mailFrom);
        requireValue("SPRING_MAIL_HOST", mailHost);
        requireValue("SPRING_MAIL_USERNAME", mailUsername);
        requireValue("SPRING_MAIL_PASSWORD", mailPassword);

        // Phone/SMS OTP is optional and costs money. DISABLED is the default.
        if ("CONSOLE".equalsIgnoreCase(phoneDelivery)) {
            throw new IllegalStateException("OTP_PHONE_DELIVERY must be DISABLED or SMS in production");
        }
        if (!"DISABLED".equalsIgnoreCase(phoneDelivery) && !"SMS".equalsIgnoreCase(phoneDelivery)) {
            throw new IllegalStateException("OTP_PHONE_DELIVERY must be DISABLED or SMS in production");
        }
        if ("SMS".equalsIgnoreCase(phoneDelivery)) {
            requireValue("MSG91_AUTH_KEY", msg91AuthKey);
            requireValue("MSG91_TEMPLATE_ID", msg91TemplateId);
        }
    }

    private void requireValue(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set in production");
        }
    }

    private void requireStrongSecret(String name, String value) {
        if (value == null || value.length() < 32 || value.contains("dev-only")) {
            throw new IllegalStateException(name + " must be at least 32 characters and must not be a development default");
        }
    }
}

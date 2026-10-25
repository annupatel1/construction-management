package com.construction.construction_management.otp;

import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.exception.OtpDeliveryException;
import com.construction.construction_management.util.Identifier;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Delivers an OTP. How it is delivered is decided purely by configuration:
 *   otp.delivery.phone = CONSOLE | SMS      (SMS uses MSG91)
 *   otp.delivery.email = CONSOLE | EMAIL    (EMAIL uses SMTP)
 * CONSOLE prints the code in the server log - for development only.
 */
@Component
public class OtpDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OtpDispatcher.class);

    private final String phoneMode;
    private final String emailMode;
    private final String mailFrom;
    private final String msg91AuthKey;
    private final String msg91TemplateId;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final RestClient restClient = RestClient.create();

    public OtpDispatcher(@Value("${otp.delivery.phone:CONSOLE}") String phoneMode,
                         @Value("${otp.delivery.email:CONSOLE}") String emailMode,
                         @Value("${otp.mail.from:no-reply@localhost}") String mailFrom,
                         @Value("${otp.sms.msg91.auth-key:}") String msg91AuthKey,
                         @Value("${otp.sms.msg91.template-id:}") String msg91TemplateId,
                         ObjectProvider<JavaMailSender> mailSender) {
        this.phoneMode = phoneMode.trim().toUpperCase();
        this.emailMode = emailMode.trim().toUpperCase();
        this.mailFrom = mailFrom;
        this.msg91AuthKey = msg91AuthKey;
        this.msg91TemplateId = msg91TemplateId;
        this.mailSender = mailSender;
    }

    @PostConstruct
    void logConfiguration() {
        log.info("OTP delivery -> phone: {}, email: {}", phoneMode, emailMode);
        if ("CONSOLE".equals(phoneMode) || "CONSOLE".equals(emailMode)) {
            log.warn("OTP CONSOLE delivery is ON: codes are printed in this log. Use it for development only.");
        }
    }

    public void send(Identifier to, OtpPurpose purpose, String code, int expiryMinutes) {
        if (to.email()) {
            if ("EMAIL".equals(emailMode)) {
                sendEmail(to.value(), purpose, code, expiryMinutes);
            } else {
                printToConsole(to, purpose, code);
            }
        } else {
            if ("SMS".equals(phoneMode)) {
                sendSms(to.value(), code, expiryMinutes);
            } else {
                printToConsole(to, purpose, code);
            }
        }
    }

    private void printToConsole(Identifier to, OtpPurpose purpose, String code) {
        log.warn("[DEV OTP] {} for {} -> {}", purpose, to.value(), code);
    }

    // ---------------- Email (SMTP) ----------------
    private void sendEmail(String to, OtpPurpose purpose, String code, int expiryMinutes) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new OtpDeliveryException("Email delivery is switched on but SMTP is not configured (spring.mail.*)");
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject("Your verification code: " + code);
            msg.setText("Your one-time code is " + code + ".\n\n"
                    + "It is valid for " + expiryMinutes + " minutes. Do not share it with anyone.\n"
                    + "If you did not request this, you can ignore this email.");
            sender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            throw new OtpDeliveryException("Could not send the email right now. Please try again.", e);
        }
    }

    // ---------------- SMS (MSG91) ----------------
    private void sendSms(String phone, String code, int expiryMinutes) {
        if (msg91AuthKey.isBlank() || msg91TemplateId.isBlank()) {
            throw new OtpDeliveryException("SMS delivery is switched on but MSG91 is not configured");
        }
        try {
            // MSG91 expects the number with country code and no '+'
            restClient.post()
                    .uri("https://control.msg91.com/api/v5/otp"
                            + "?template_id={tpl}&mobile={mobile}&authkey={key}&otp={otp}&otp_expiry={exp}&otp_length=6",
                            msg91TemplateId, "91" + phone, msg91AuthKey, code, expiryMinutes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send OTP SMS", e);
            throw new OtpDeliveryException("Could not send the SMS right now. Please try again.", e);
        }
    }
}

package com.construction.construction_management.otp;

import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.OtpDeliveryException;
import com.construction.construction_management.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpDispatcherTest {

    private OtpDispatcher dispatcher(String phoneMode, String emailMode) {
        // The JavaMailSender provider is only needed for the EMAIL path.
        return new OtpDispatcher(phoneMode, emailMode, "no-reply@example.com", "", "", null);
    }

    @Test
    void phoneOtpIsRejectedWithAClearMessageWhenDisabled() {
        OtpDispatcher dispatcher = dispatcher("DISABLED", "CONSOLE");

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> dispatcher.send(Identifier.phone("9876543210"), OtpPurpose.LOGIN, "123456", 5));

        assertTrue(error.getMessage().contains("Phone OTP is not enabled"));
    }

    @Test
    void consoleDeliveryWorksForDevelopment() {
        assertDoesNotThrow(() -> dispatcher("CONSOLE", "CONSOLE")
                .send(Identifier.email("dev@example.com"), OtpPurpose.LOGIN, "123456", 5));
    }

    @Test
    void missingEmailDeliveryConfigurationFailsLoudly() {
        assertThrows(OtpDeliveryException.class, () -> dispatcher("DISABLED", "DISABLED")
                .send(Identifier.email("dev@example.com"), OtpPurpose.LOGIN, "123456", 5));
    }
}

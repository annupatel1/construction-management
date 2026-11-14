package com.construction.construction_management.otp;

import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.OtpDeliveryException;
import com.construction.construction_management.util.Identifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtpDispatcherTest {

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> provider(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        return provider;
    }

    private OtpDispatcher dispatcher(String phoneMode, String emailMode) {
        return new OtpDispatcher(phoneMode, emailMode, "no-reply@example.com", "", "", provider(null));
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
    void emailModeSendsThroughSmtp() {
        JavaMailSender sender = mock(JavaMailSender.class);
        OtpDispatcher dispatcher = new OtpDispatcher(
                "DISABLED", "EMAIL", "no-reply@example.com", "", "", provider(sender));

        dispatcher.send(Identifier.email("user@example.com"), OtpPurpose.LOGIN, "123456", 5);

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertEquals("no-reply@example.com", message.getValue().getFrom());
        assertArrayEquals(new String[]{"user@example.com"}, message.getValue().getTo());
        assertTrue(message.getValue().getSubject().contains("123456"));
        assertTrue(message.getValue().getText().contains("5 minutes"));
    }

    @Test
    void emailModeWithoutSmtpFailsLoudly() {
        assertThrows(OtpDeliveryException.class, () -> dispatcher("DISABLED", "EMAIL")
                .send(Identifier.email("dev@example.com"), OtpPurpose.LOGIN, "123456", 5));
    }

    @Test
    void missingEmailDeliveryConfigurationFailsLoudly() {
        assertThrows(OtpDeliveryException.class, () -> dispatcher("DISABLED", "DISABLED")
                .send(Identifier.email("dev@example.com"), OtpPurpose.LOGIN, "123456", 5));
    }
}

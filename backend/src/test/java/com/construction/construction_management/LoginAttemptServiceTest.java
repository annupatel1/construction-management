package com.construction.construction_management;

import com.construction.construction_management.exception.TooManyRequestsException;
import com.construction.construction_management.security.LoginAttemptService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    @Test
    void blocksAfterTheConfiguredNumberOfFailuresAndClearsOnSuccess() {
        LoginAttemptService service = new LoginAttemptService(2, 60);
        String key = "phone:9999999999";

        service.recordFailure(key);
        service.recordFailure(key);
        assertThrows(TooManyRequestsException.class, () -> service.checkAllowed(key));

        service.clear(key);
        assertDoesNotThrow(() -> service.checkAllowed(key));
    }
}

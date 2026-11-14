package com.construction.construction_management;

import com.construction.construction_management.entity.AccountStatus;
import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.otp.OtpDispatcher;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.util.Identifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Email OTP is the free delivery channel. A user may still type their mobile
 * number, but the code must go to the email on file whenever one exists.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmailOtpIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    @MockitoBean OtpDispatcher otpDispatcher;

    private User user(String phone, String email) {
        Role role = roleRepository.findByName(RoleName.CONTRACTOR).orElseThrow();
        User user = new User();
        user.setName("Email OTP " + phone);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword("not-used-in-these-tests");
        user.setAddress("Indore");
        user.setRole(role);
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.saveAndFlush(user);
    }

    @Test
    void loginOtpRequestedWithAPhoneIsDeliveredToEmailAndVerifiesWithThePhone() throws Exception {
        User user = user("9812345678", "contractor-otp@example.com");

        mockMvc.perform(post("/api/auth/login-otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"9812345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentTo").value("the email on your account"));

        ArgumentCaptor<Identifier> destination = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(otpDispatcher).send(destination.capture(), eq(OtpPurpose.LOGIN), code.capture(), anyInt());

        assertTrue(destination.getValue().email(), "OTP should be delivered by email");
        assertEquals(user.getEmail(), destination.getValue().value());

        // The user still verifies with the number they typed.
        mockMvc.perform(post("/api/auth/login-otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"9812345678\",\"otp\":\"" + code.getValue() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(user.getId()));
    }

    @Test
    void passwordResetRequestedWithAPhoneIsDeliveredToEmailAndResetsWithThePhone() throws Exception {
        User user = user("9812345679", "reset-otp@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"9812345679\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentTo").value("the email on your account"));

        ArgumentCaptor<Identifier> destination = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(otpDispatcher).send(destination.capture(), eq(OtpPurpose.RESET_PASSWORD), code.capture(), anyInt());

        assertTrue(destination.getValue().email(), "OTP should be delivered by email");
        assertEquals(user.getEmail(), destination.getValue().value());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"9812345679\",\"otp\":\"" + code.getValue()
                                + "\",\"newPassword\":\"NewStrongPass123!\"}"))
                .andExpect(status().isOk());
    }
}

package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.*;
import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.otp.OtpService;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.security.JwtUtil;
import com.construction.construction_management.service.AuthService;
import com.construction.construction_management.util.Identifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_SENT = "If this account exists, a code has been sent.";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final CurrentUser currentUser;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           OtpService otpService,
                           CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.currentUser = currentUser;
    }

    // ------------------------------------------------------------------ helpers

    private OtpSentResponse sent(String message, Identifier to) {
        return new OtpSentResponse(message, to.masked(), otpService.expirySeconds(), otpService.cooldownSeconds());
    }

    private AuthResponse authResponse(User user, String message) {
        return new AuthResponse(jwtUtil.generateToken(user.getId()), message, UserResponse.from(user));
    }

    private Optional<User> find(Identifier id) {
        return id.email() ? userRepository.findByEmail(id.value()) : userRepository.findByPhone(id.value());
    }

    /** A user may use an identifier for OTP login / reset only if that identifier is verified. */
    private boolean eligible(User user, Identifier id) {
        return user.isPhoneVerified() && (id.isPhone() || user.isEmailVerified());
    }

    /**
     * Gives the email to this user. An email that another account holds but never verified is taken
     * over, so nobody can block someone else's email by registering it first.
     */
    private void claimEmail(User owner, String email) {
        userRepository.findByEmail(email).ifPresent(other -> {
            if (other.getId() != owner.getId()) {
                if (other.isEmailVerified()) {
                    throw new ConflictException("This email is already registered");
                }
                other.setEmail(null);
                userRepository.saveAndFlush(other);
            }
        });
    }

    // ------------------------------------------------------------ registration

    @Override
    @Transactional
    public OtpSentResponse register(RegisterRequest request) {
        if (request.getRoleName() == RoleName.ADMIN) {
            throw new BadRequestException("Invalid role selected");
        }
        Identifier phone = Identifier.phone(request.getPhone());

        User user = userRepository.findByPhone(phone.value()).orElse(null);
        if (user != null && user.isPhoneVerified()) {
            throw new ConflictException("This phone number is already registered. Please log in.");
        }
        if (user == null) {
            user = new User();
            user.setPhone(phone.value());
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setName(request.getName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress().trim());
        user.setRole(role);
        user.setPhoneVerified(false);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = Identifier.email(request.getEmail()).value();
            claimEmail(user, email);
            user.setEmail(email);
        } else {
            user.setEmail(null);
        }
        user.setEmailVerified(false);

        userRepository.save(user);
        otpService.issue(phone, OtpPurpose.REGISTER, true);
        return sent("We have sent a 6-digit code to verify your mobile number.", phone);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public AuthResponse verifyRegistration(VerifyOtpRequest request) {
        Identifier phone = Identifier.phone(request.getIdentifier());
        otpService.verify(phone, OtpPurpose.REGISTER, request.getOtp());

        User user = userRepository.findByPhone(phone.value())
                .orElseThrow(() -> new BadRequestException("Invalid or expired code. Please request a new one."));
        user.setPhoneVerified(true);
        userRepository.save(user);
        return authResponse(user, "Mobile number verified. Welcome!");
    }

    @Override
    @Transactional
    public OtpSentResponse resendRegistrationOtp(OtpRequest request) {
        Identifier phone = Identifier.phone(request.getIdentifier());
        boolean pending = userRepository.findByPhone(phone.value())
                .map(u -> !u.isPhoneVerified())
                .orElse(false);
        otpService.issue(phone, OtpPurpose.REGISTER, pending);
        return sent(GENERIC_SENT, phone);
    }

    // ------------------------------------------------------------ password login

    @Override
    public AuthResponse login(LoginRequest request) {
        Identifier id = Identifier.parse(request.getIdentifier());

        User user = find(id).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (id.email() && !user.isEmailVerified()) {
            // an unverified email cannot be used to log in
            throw new BadCredentialsException("Invalid credentials");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            // a worker added by a contractor has no password until they register
            throw new BadCredentialsException("Invalid credentials");
        }

        // Throws BadCredentialsException for a wrong password, DisabledException if the phone is not verified yet.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getPhone(), request.getPassword()));

        return authResponse(user, "Login successful");
    }

    // ------------------------------------------------------------ login with OTP

    @Override
    @Transactional
    public OtpSentResponse requestLoginOtp(OtpRequest request) {
        return sendIfEligible(request, OtpPurpose.LOGIN);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        Identifier id = Identifier.parse(request.getIdentifier());
        otpService.verify(id, OtpPurpose.LOGIN, request.getOtp());

        User user = find(id)
                .filter(u -> eligible(u, id))
                .orElseThrow(() -> new BadRequestException("Invalid or expired code. Please request a new one."));
        return authResponse(user, "Login successful");
    }

    // ------------------------------------------------------------ forgot password

    @Override
    @Transactional
    public OtpSentResponse forgotPassword(OtpRequest request) {
        return sendIfEligible(request, OtpPurpose.RESET_PASSWORD);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Identifier id = Identifier.parse(request.getIdentifier());
        otpService.verify(id, OtpPurpose.RESET_PASSWORD, request.getOtp());

        User user = find(id)
                .filter(u -> eligible(u, id))
                .orElseThrow(() -> new BadRequestException("Invalid or expired code. Please request a new one."));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password changed. You can now log in with your new password.");
    }

    /**
     * Shared by "login with OTP" and "forgot password". The answer is the same whether or not the
     * account exists, so these endpoints cannot be used to find out who is registered.
     */
    private OtpSentResponse sendIfEligible(OtpRequest request, OtpPurpose purpose) {
        Identifier id = Identifier.parse(request.getIdentifier());
        boolean deliver = find(id).map(u -> eligible(u, id)).orElse(false);
        otpService.issue(id, purpose, deliver);
        return sent(GENERIC_SENT, id);
    }

    // ------------------------------------------------------------ email management

    @Override
    @Transactional
    public OtpSentResponse changeEmail(EmailRequest request) {
        Identifier email = Identifier.email(request.getEmail());
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        claimEmail(user, email.value());
        user.setEmail(email.value());
        user.setEmailVerified(false);
        userRepository.save(user);

        otpService.issue(email, OtpPurpose.EMAIL_VERIFY, true);
        return sent("We have sent a 6-digit code to verify your email.", email);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public UserResponse verifyEmail(OtpOnlyRequest request) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getEmail() == null) {
            throw new BadRequestException("Add an email address first");
        }
        otpService.verify(Identifier.email(user.getEmail()), OtpPurpose.EMAIL_VERIFY, request.getOtp());
        user.setEmailVerified(true);
        return UserResponse.from(userRepository.save(user));
    }
}

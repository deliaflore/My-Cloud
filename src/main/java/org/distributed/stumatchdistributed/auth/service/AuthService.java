package org.distributed.stumatchdistributed.auth.service;

import org.distributed.stumatchdistributed.auth.dto.AuthResponse;
import org.distributed.stumatchdistributed.auth.dto.LoginRequest;
import org.distributed.stumatchdistributed.auth.dto.OtpVerificationRequest;
import org.distributed.stumatchdistributed.auth.dto.RegisterRequest;
import org.distributed.stumatchdistributed.auth.entity.OtpPurpose;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.repository.UserAccountRepository;
import org.distributed.stumatchdistributed.storage.service.UserStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;
    private final UserStorageService userStorageService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userAccountRepository,
                       UserStorageService userStorageService,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       NotificationService notificationService,
                       JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.userStorageService = userStorageService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.notificationService = notificationService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userAccountRepository.findByEmailIgnoreCase(request.email())
                .ifPresent(user -> {
                    throw new IllegalStateException("Email already registered");
                });

        UserAccount user = UserAccount.builder()
                .email(request.email().toLowerCase())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userAccountRepository.save(user);

        userStorageService.provision(user);

        var token = otpService.generateOtp(user, OtpPurpose.REGISTRATION);
        notificationService.sendOtpEmail(user.getEmail(), token.getCode(), "account verification");

        return new AuthResponse(true,
                "Registration successful. Please verify the OTP sent to your email to activate your storage.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        OtpPurpose purpose = user.isEmailVerified() ? OtpPurpose.LOGIN : OtpPurpose.REGISTRATION;
        var token = otpService.generateOtp(user, purpose);
        notificationService.sendOtpEmail(user.getEmail(), token.getCode(),
                user.isEmailVerified() ? "login verification" : "account verification");

        return new AuthResponse(true,
                user.isEmailVerified()
                        ? "OTP sent to your email. Please confirm to finish login."
                        : "Your account is pending verification. Please enter the OTP sent to your email.");
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        OtpPurpose purpose = user.isEmailVerified() ? OtpPurpose.LOGIN : OtpPurpose.REGISTRATION;

        boolean verified = otpService.verifyOtp(user, request.code(), purpose);
        if (!verified) {
            throw new IllegalArgumentException("Invalid or expired OTP code");
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            log.info("User {} email verified", user.getEmail());
        }

        user.setLastLoginAt(LocalDateTime.now());
        userAccountRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(true, "OTP verified successfully.", token);
    }

}


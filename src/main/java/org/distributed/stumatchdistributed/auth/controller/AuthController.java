package org.distributed.stumatchdistributed.auth.controller;

import jakarta.validation.Valid;
import org.distributed.stumatchdistributed.auth.dto.AuthResponse;
import org.distributed.stumatchdistributed.auth.dto.LoginRequest;
import org.distributed.stumatchdistributed.auth.dto.OtpVerificationRequest;
import org.distributed.stumatchdistributed.auth.dto.RegisterRequest;
import org.distributed.stumatchdistributed.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for {}", request.email());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        log.info("OTP verification for {}", request.email());
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}


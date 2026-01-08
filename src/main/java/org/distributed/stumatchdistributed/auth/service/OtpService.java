package org.distributed.stumatchdistributed.auth.service;

import org.distributed.stumatchdistributed.auth.entity.OtpPurpose;
import org.distributed.stumatchdistributed.auth.entity.OtpToken;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.repository.OtpTokenRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_MINUTES = 5L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepository otpTokenRepository;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    public OtpToken generateOtp(UserAccount user, OtpPurpose purpose) {
        String code = generateNumericCode();

        OtpToken token = OtpToken.builder()
                .user(user)
                .code(code)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        return otpTokenRepository.save(token);
    }

    public boolean verifyOtp(UserAccount user, String code, OtpPurpose purpose) {
        return otpTokenRepository
                .findFirstByUserIdAndPurposeAndUsedIsFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        user.getId(),
                        purpose,
                        LocalDateTime.now()
                )
                .filter(token -> token.getCode().equals(code))
                .map(token -> {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }

    private String generateNumericCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int number = RANDOM.nextInt(bound / 10, bound);
        return Integer.toString(number);
    }
}


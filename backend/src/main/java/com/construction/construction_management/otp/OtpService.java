package com.construction.construction_management.otp;

import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.entity.OtpToken;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.TooManyRequestsException;
import com.construction.construction_management.repository.OtpTokenRepository;
import com.construction.construction_management.util.Identifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepository repository;
    private final OtpDispatcher dispatcher;
    private final String secret;
    private final int expiryMinutes;
    private final int maxAttempts;
    private final int cooldownSeconds;
    private final int maxPerHour;

    public OtpService(OtpTokenRepository repository,
                      OtpDispatcher dispatcher,
                      @Value("${otp.secret}") String secret,
                      @Value("${otp.expiry-minutes:5}") int expiryMinutes,
                      @Value("${otp.max-attempts:5}") int maxAttempts,
                      @Value("${otp.resend-cooldown-seconds:30}") int cooldownSeconds,
                      @Value("${otp.max-per-hour:5}") int maxPerHour) {
        this.repository = repository;
        this.dispatcher = dispatcher;
        this.secret = secret;
        this.expiryMinutes = expiryMinutes;
        this.maxAttempts = maxAttempts;
        this.cooldownSeconds = cooldownSeconds;
        this.maxPerHour = maxPerHour;
    }

    public int expirySeconds() {
        return expiryMinutes * 60;
    }

    public int cooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * Creates and (if deliver is true) sends a new code. When deliver is false a "ghost" token is
     * recorded instead, so rate limits behave identically whether or not the account exists
     * and callers cannot use this to discover which phone numbers / emails are registered.
     */
    @Transactional
    public void issue(Identifier id, OtpPurpose purpose, boolean deliver) {
        LocalDateTime now = LocalDateTime.now();

        repository.findFirstByIdentifierOrderByCreatedAtDesc(id.value()).ifPresent(last -> {
            LocalDateTime next = last.getCreatedAt().plusSeconds(cooldownSeconds);
            if (next.isAfter(now)) {
                long wait = Math.max(1, Duration.between(now, next).toSeconds());
                throw new TooManyRequestsException("Please wait " + wait + " seconds before requesting another code");
            }
        });
        if (repository.countByIdentifierAndCreatedAtAfter(id.value(), now.minusHours(1)) >= maxPerHour) {
            throw new TooManyRequestsException("Too many code requests. Please try again after some time.");
        }

        // a newer code replaces any older unused one for the same purpose
        var older = repository.findByIdentifierAndPurposeAndConsumedFalse(id.value(), purpose);
        older.forEach(t -> t.setConsumed(true));
        repository.saveAll(older);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpToken token = new OtpToken();
        token.setIdentifier(id.value());
        token.setPurpose(purpose);
        token.setCodeHash(hash(id.value(), purpose, code));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusMinutes(expiryMinutes));
        token.setConsumed(!deliver); // ghost tokens can never be verified
        repository.save(token);

        if (deliver) {
            dispatcher.send(id, purpose, code, expiryMinutes);
        }
    }

    /** Throws BadRequestException unless the code is correct. Wrong attempts are counted and persisted. */
    @Transactional(noRollbackFor = BadRequestException.class)
    public void verify(Identifier id, OtpPurpose purpose, String code) {
        OtpToken token = repository
                .findFirstByIdentifierAndPurposeAndConsumedFalseOrderByCreatedAtDesc(id.value(), purpose)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code. Please request a new one."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setConsumed(true);
            repository.save(token);
            throw new BadRequestException("This code has expired. Please request a new one.");
        }

        token.setAttempts(token.getAttempts() + 1);
        if (token.getAttempts() > maxAttempts) {
            token.setConsumed(true);
            repository.save(token);
            throw new BadRequestException("Too many wrong attempts. Please request a new code.");
        }

        boolean match = MessageDigest.isEqual(
                hash(id.value(), purpose, code).getBytes(StandardCharsets.UTF_8),
                token.getCodeHash().getBytes(StandardCharsets.UTF_8));
        if (!match) {
            repository.save(token);
            throw new BadRequestException("Incorrect code. Please try again.");
        }

        token.setConsumed(true);
        repository.save(token);
    }

    private String hash(String identifier, OtpPurpose purpose, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal((identifier + "|" + purpose + "|" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash OTP", e);
        }
    }

    /** Housekeeping: delete tokens that expired more than a day ago. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldTokens() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now().minusDays(1));
    }
}

package com.construction.construction_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** One issued OTP. Only a keyed hash of the code is stored, never the code itself. */
@Getter
@Setter
@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_identifier", columnList = "identifier")
})
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    /** Normalised phone (10 digits) or lower-case email. */
    @Column(nullable = false)
    String identifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OtpPurpose purpose;

    @Column(nullable = false)
    String codeHash;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    int attempts;

    /** True once used, replaced by a newer code, or if it was never actually sent. */
    boolean consumed;
}

package com.construction.construction_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;

    @Column(unique = true)
    String email;

    String password;

    @Column(nullable = false, unique = true, length = 10)
    String phone;

    String address;

    /** Account is active only after the phone OTP is verified. */
    boolean phoneVerified;

    /** Email can be used to log in / reset password only once verified. */
    boolean emailVerified;

    /** Suspended or deleted accounts cannot log in, even with a valid password. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AccountStatus accountStatus = AccountStatus.ACTIVE;

    /**
     * Incremented on logout / password reset / suspend so every token issued before that moment
     * stops working. Starts at 1: tokens issued before this field existed have no version claim and
     * are treated as version 0, so they are rejected after the Gate 0 migration.
     */
    @Column(nullable = false)
    int tokenVersion = 1;

    @ManyToOne
    Role role;

    /** Set when a contractor adds a worker who has not registered in the app yet. */
    @ManyToOne
    User createdByContractor;
}

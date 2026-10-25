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

    @ManyToOne
    Role role;
}

package com.construction.construction_management.repository;

import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    List<User> findByRoleNameAndPhoneVerifiedTrue(RoleName roleName);
}

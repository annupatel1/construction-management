package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleName name);
}
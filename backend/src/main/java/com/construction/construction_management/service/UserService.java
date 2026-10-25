package com.construction.construction_management.service;

import org.springframework.data.domain.Pageable;

import com.construction.construction_management.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(int id);
    UserResponse getCurrentUser();
}

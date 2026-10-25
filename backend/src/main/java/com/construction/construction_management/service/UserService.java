package com.construction.construction_management.service;

import com.construction.construction_management.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(int id);
    UserResponse getCurrentUser();
}

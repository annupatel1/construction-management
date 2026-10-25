package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.UserResponse;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public UserServiceImpl(UserRepository userRepository, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Override
    public List<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from).getContent();
    }

    @Override
    public UserResponse getUserById(int id) {
        if (id != currentUser.id() && !currentUser.isAdmin()) {
            throw new AccessDeniedException("No access to this user");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        return UserResponse.from(currentUser.get());
    }
}

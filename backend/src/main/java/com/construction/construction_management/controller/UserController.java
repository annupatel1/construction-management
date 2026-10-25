package com.construction.construction_management.controller;

import com.construction.construction_management.dto.UserResponse;
import com.construction.construction_management.util.Pages;
import com.construction.construction_management.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** The logged-in user - the frontend calls this on page load to restore the session. */
    @GetMapping("/me")
    public UserResponse me() {
        return userService.getCurrentUser();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return userService.getAllUsers(Pages.of(page, size));
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }
}

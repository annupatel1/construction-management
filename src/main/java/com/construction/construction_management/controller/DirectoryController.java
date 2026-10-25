package com.construction.construction_management.controller;

import com.construction.construction_management.dto.DirectoryUserResponse;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/** Lets owners find contractors and contractors find labour, without exposing the full user list. */
@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final UserRepository userRepository;

    public DirectoryController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','ADMIN')")
    @GetMapping("/contractors")
    public List<DirectoryUserResponse> contractors(@RequestParam(required = false) String q) {
        return search(RoleName.CONTRACTOR, q);
    }

    @PreAuthorize("hasAnyRole('CONTRACTOR','ADMIN')")
    @GetMapping("/labourers")
    public List<DirectoryUserResponse> labourers(@RequestParam(required = false) String q) {
        return search(RoleName.LABOUR, q);
    }

    /** q matches name or address (city / area), case-insensitive. */
    private List<DirectoryUserResponse> search(RoleName role, String q) {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByRoleNameAndPhoneVerifiedTrue(role).stream()
                .filter(u -> needle.isEmpty()
                        || (u.getName() != null && u.getName().toLowerCase(Locale.ROOT).contains(needle))
                        || (u.getAddress() != null && u.getAddress().toLowerCase(Locale.ROOT).contains(needle)))
                .map(DirectoryUserResponse::from)
                .toList();
    }
}

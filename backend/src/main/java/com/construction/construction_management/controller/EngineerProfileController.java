package com.construction.construction_management.controller;

import com.construction.construction_management.dto.EngineerDirectoryResponse;
import com.construction.construction_management.dto.EngineerProfileRequest;
import com.construction.construction_management.dto.EngineerSelfResponse;
import com.construction.construction_management.service.EngineerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engineers")
public class EngineerProfileController {

    private final EngineerProfileService profileService;

    public EngineerProfileController(EngineerProfileService profileService) {
        this.profileService = profileService;
    }

    @PreAuthorize("hasRole('ENGINEER')")
    @GetMapping("/me")
    public EngineerSelfResponse getMine() {
        return profileService.getMine();
    }

    @PreAuthorize("hasRole('ENGINEER')")
    @PutMapping("/me")
    public EngineerSelfResponse saveMine(@Valid @RequestBody EngineerProfileRequest request) {
        return profileService.saveMine(request);
    }

    /** Public profile, addressed by user id so invitations and directory cards agree. */
    @GetMapping("/{engineerId}")
    public EngineerDirectoryResponse getPublic(@PathVariable int engineerId) {
        return profileService.getPublic(engineerId);
    }
}

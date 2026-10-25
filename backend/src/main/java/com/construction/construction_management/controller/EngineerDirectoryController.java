package com.construction.construction_management.controller;

import com.construction.construction_management.dto.EngineerDirectoryResponse;
import com.construction.construction_management.entity.EngineerAvailability;
import com.construction.construction_management.service.EngineerProfileService;
import com.construction.construction_management.util.Pages;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Searchable engineer directory. Contact details are deliberately not part of this payload. */
@RestController
@RequestMapping("/api/directory/engineers")
public class EngineerDirectoryController {

    private final EngineerProfileService profileService;

    public EngineerDirectoryController(EngineerProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<EngineerDirectoryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) EngineerAvailability availability,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return profileService.search(q, city, specialty, minExperience, availability,
                Pages.of(page, size));
    }
}

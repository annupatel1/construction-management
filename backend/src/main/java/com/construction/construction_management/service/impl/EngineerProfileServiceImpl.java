package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.EngineerDirectoryResponse;
import com.construction.construction_management.dto.EngineerProfileRequest;
import com.construction.construction_management.dto.EngineerSelfResponse;
import com.construction.construction_management.entity.EngineerAvailability;
import com.construction.construction_management.entity.EngineerProfile;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.EngineerProfileRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.EngineerProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class EngineerProfileServiceImpl implements EngineerProfileService {

    private final EngineerProfileRepository profileRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public EngineerProfileServiceImpl(EngineerProfileRepository profileRepository,
                                      CurrentUser currentUser,
                                      AuditService auditService) {
        this.profileRepository = profileRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public EngineerSelfResponse getMine() {
        User user = currentUser.get();
        EngineerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complete your engineer profile first"));
        return self(profile, user);
    }

    @Override
    @Transactional
    public EngineerSelfResponse saveMine(EngineerProfileRequest request) {
        User user = currentUser.get();
        if (user.getRole() == null || user.getRole().getName() != RoleName.ENGINEER) {
            throw new BadRequestException("Only engineers can edit an engineer profile");
        }

        EngineerProfile profile = profileRepository.findByUserId(user.getId()).orElseGet(() -> {
            EngineerProfile created = new EngineerProfile();
            created.setUser(user);
            return created;
        });

        profile.setSpecialty(request.getSpecialty().trim());
        profile.setSkills(clean(request.getSkills()));
        profile.setExperienceYears(request.getExperienceYears());
        profile.setCity(request.getCity().trim());
        profile.setServiceArea(clean(request.getServiceArea()));
        profile.setLanguages(clean(request.getLanguages()));
        profile.setAvailability(request.getAvailability());
        profile.setBio(clean(request.getBio()));
        profile.setPortfolioUrl(clean(request.getPortfolioUrl()));
        profile.setUpdatedAt(LocalDateTime.now());

        EngineerProfile saved = profileRepository.save(profile);
        auditService.record("ENGINEER_PROFILE_UPDATED", "EngineerProfile", saved.getId(),
                "specialty=" + saved.getSpecialty() + "; city=" + saved.getCity());
        return self(saved, user);
    }

    @Override
    @Transactional(readOnly = true)
    public EngineerDirectoryResponse getPublic(int engineerId) {
        EngineerProfile profile = profileRepository.findByUserId(engineerId)
                .orElseThrow(() -> new ResourceNotFoundException("Engineer not found"));
        return directory(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EngineerDirectoryResponse> search(String q, String city, String specialty,
                                                  Integer minExperience,
                                                  EngineerAvailability availability,
                                                  Pageable pageable) {
        Page<EngineerProfile> results = profileRepository.search(
                normalize(q), normalize(city), normalize(specialty), minExperience, availability, pageable);
        return results.map(this::directory).getContent();
    }

    private EngineerDirectoryResponse directory(EngineerProfile p) {
        return new EngineerDirectoryResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getName(),
                p.getSpecialty(),
                csv(p.getSkills()),
                p.getExperienceYears(),
                p.getCity(),
                p.getServiceArea(),
                csv(p.getLanguages()),
                p.getAvailability().name(),
                p.getBio(),
                p.getPortfolioUrl(),
                p.getCredentialStatus().name(),
                complete(p)
        );
    }

    private EngineerSelfResponse self(EngineerProfile p, User user) {
        return new EngineerSelfResponse(
                p.getId(),
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                p.getSpecialty(),
                csv(p.getSkills()),
                p.getExperienceYears(),
                p.getCity(),
                p.getServiceArea(),
                csv(p.getLanguages()),
                p.getAvailability().name(),
                p.getBio(),
                p.getPortfolioUrl(),
                p.getCredentialStatus().name(),
                complete(p)
        );
    }

    private static boolean complete(EngineerProfile p) {
        return p.getSpecialty() != null && !p.getSpecialty().isBlank()
                && p.getCity() != null && !p.getCity().isBlank()
                && p.getSkills() != null && !p.getSkills().isBlank()
                && p.getServiceArea() != null && !p.getServiceArea().isBlank();
    }

    private static List<String> csv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}

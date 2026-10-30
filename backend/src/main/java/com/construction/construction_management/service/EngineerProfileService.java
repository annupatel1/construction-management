package com.construction.construction_management.service;

import com.construction.construction_management.dto.EngineerDirectoryResponse;
import com.construction.construction_management.dto.EngineerProfileRequest;
import com.construction.construction_management.dto.EngineerSelfResponse;
import com.construction.construction_management.entity.EngineerAvailability;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EngineerProfileService {
    EngineerSelfResponse getMine();

    EngineerSelfResponse saveMine(EngineerProfileRequest request);

    EngineerDirectoryResponse getPublic(int engineerId);

    List<EngineerDirectoryResponse> search(String q, String city, String specialty,
                                           Integer minExperience, EngineerAvailability availability,
                                           Pageable pageable);
}

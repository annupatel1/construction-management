package com.construction.construction_management.controller;

import com.construction.construction_management.dto.CreateLabourRequest;
import com.construction.construction_management.dto.DirectoryUserResponse;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Lets owners find contractors and contractors find labour, without exposing the full user list. */
@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrentUser currentUser;

    public DirectoryController(UserRepository userRepository,
                               RoleRepository roleRepository,
                               CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','ADMIN')")
    @GetMapping("/contractors")
    public List<DirectoryUserResponse> contractors(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return search(RoleName.CONTRACTOR, q, false, page, size);
    }

    /** Registered labourers plus the workers this contractor has added themselves. */
    @PreAuthorize("hasAnyRole('CONTRACTOR','ADMIN')")
    @GetMapping("/labourers")
    public List<DirectoryUserResponse> labourers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return search(RoleName.LABOUR, q, currentUser.role() == RoleName.CONTRACTOR, page, size);
    }

    /**
     * A contractor adds a worker who does not use the app yet. If the same phone number is already
     * a registered labourer, that account is returned instead of creating a duplicate.
     */
    @PreAuthorize("hasRole('CONTRACTOR')")
    @PostMapping("/labourers")
    public DirectoryUserResponse addLabourer(@Valid @RequestBody CreateLabourRequest request) {
        User contractor = currentUser.get();
        String phone = request.getPhone().trim();

        User labour = userRepository.findByPhone(phone).orElse(null);
        if (labour != null) {
            if (labour.getRole().getName() != RoleName.LABOUR) {
                throw new ConflictException("This mobile number already belongs to a different account");
            }
            if (labour.isPhoneVerified()) {
                return DirectoryUserResponse.from(labour);
            }
            if (labour.getCreatedByContractor() != null
                    && labour.getCreatedByContractor().getId() != contractor.getId()) {
                throw new ConflictException("This worker is already added by another contractor");
            }
            if (labour.getCreatedByContractor() == null) {
                labour.setCreatedByContractor(contractor);
                userRepository.save(labour);
            }
            return DirectoryUserResponse.from(labour);
        }

        Role labourRole = roleRepository.findByName(RoleName.LABOUR)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User worker = new User();
        worker.setName(request.getName().trim());
        worker.setPhone(phone);
        worker.setAddress(request.getAddress() == null ? "" : request.getAddress().trim());
        worker.setRole(labourRole);
        // No password yet: this worker cannot log in until they register with this phone number.
        worker.setPhoneVerified(false);
        worker.setEmailVerified(false);
        worker.setCreatedByContractor(contractor);

        return DirectoryUserResponse.from(userRepository.save(worker));
    }

    /** q matches name or address (city / area), case-insensitive. */
    private List<DirectoryUserResponse> search(RoleName role, String q, boolean includeOwnWorkers,
                                               int page, int size) {
        var pageable = Pages.of(page, size);
        Map<Integer, User> found = new LinkedHashMap<>();
        userRepository.findByRoleNameAndPhoneVerifiedTrue(role)
                .forEach(u -> found.put(u.getId(), u));
        if (includeOwnWorkers) {
            userRepository.findByRoleNameAndCreatedByContractorId(role, currentUser.id())
                    .forEach(u -> found.put(u.getId(), u));
        }

        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return found.values().stream()
                .filter(u -> needle.isEmpty()
                        || (u.getName() != null && u.getName().toLowerCase(Locale.ROOT).contains(needle))
                        || (u.getAddress() != null && u.getAddress().toLowerCase(Locale.ROOT).contains(needle)))
                .sorted(Comparator.comparing(u -> u.getName() == null ? "" : u.getName()))
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .map(DirectoryUserResponse::from)
                .toList();
    }
}

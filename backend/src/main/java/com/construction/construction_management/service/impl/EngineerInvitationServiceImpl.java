package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.EngineerInvitationRequest;
import com.construction.construction_management.dto.EngineerInvitationResponse;
import com.construction.construction_management.dto.ProjectEngineerResponse;
import com.construction.construction_management.entity.*;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.EngineerInvitationRepository;
import com.construction.construction_management.repository.EngineerProfileRepository;
import com.construction.construction_management.repository.ProjectMembershipRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.EngineerInvitationService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EngineerInvitationServiceImpl implements EngineerInvitationService {

    private final EngineerInvitationRepository invitationRepository;
    private final EngineerProfileRepository profileRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;

    public EngineerInvitationServiceImpl(EngineerInvitationRepository invitationRepository,
                                         EngineerProfileRepository profileRepository,
                                         ProjectMembershipRepository membershipRepository,
                                         UserRepository userRepository,
                                         CurrentUser currentUser,
                                         AccessGuard guard,
                                         AuditService auditService) {
        this.invitationRepository = invitationRepository;
        this.profileRepository = profileRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public EngineerInvitationResponse invite(int projectId, EngineerInvitationRequest request) {
        Project project = guard.projectForOwnerOrContractor(projectId);
        User inviter = currentUser.get();

        User engineer = userRepository.findById(request.getEngineerId())
                .orElseThrow(() -> new ResourceNotFoundException("Engineer not found"));
        if (engineer.getRole() == null || engineer.getRole().getName() != RoleName.ENGINEER) {
            throw new BadRequestException("Selected user is not an engineer");
        }
        if (engineer.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("This engineer is not available");
        }
        if (engineer.getId() == inviter.getId()) {
            throw new BadRequestException("You cannot invite yourself");
        }
        if (!profileRepository.existsByUserId(engineer.getId())) {
            throw new BadRequestException("This engineer has not completed their profile yet");
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        if (request.getResponseDeadline() != null
                && request.getResponseDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("Response deadline cannot be in the past");
        }
        if (membershipRepository.existsByProjectIdAndUserIdAndActiveTrue(projectId, engineer.getId())) {
            throw new ConflictException("This engineer is already on the project");
        }
        if (invitationRepository.existsByProjectIdAndEngineerIdAndStatus(
                projectId, engineer.getId(), InvitationStatus.PENDING)) {
            throw new ConflictException("An invitation is already pending for this engineer");
        }

        EngineerInvitation invitation = new EngineerInvitation();
        invitation.setProject(project);
        invitation.setInviter(inviter);
        invitation.setEngineer(engineer);
        invitation.setScope(request.getScope().trim());
        invitation.setStartDate(request.getStartDate());
        invitation.setEndDate(request.getEndDate());
        invitation.setFeeTerms(clean(request.getFeeTerms()));
        // Gate 2 membership grants project access. Site actions are derived from role + active membership.
        String requested = request.getRequestedPermissions();
        if (requested != null && !requested.isBlank() && !"PROJECT_READ".equals(requested.trim())) {
            throw new BadRequestException("Only project access can be granted");
        }
        invitation.setRequestedPermissions("PROJECT_READ");
        invitation.setResponseDeadline(request.getResponseDeadline());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreatedAt(LocalDateTime.now());

        EngineerInvitation saved = invitationRepository.save(invitation);
        auditService.record("ENGINEER_INVITED", "EngineerInvitation", saved.getId(),
                "projectId=" + projectId + "; engineerId=" + engineer.getId());
        return EngineerInvitationResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EngineerInvitationResponse> inbox(Pageable pageable) {
        return invitationRepository.findByEngineerId(currentUser.id(), pageable)
                .map(EngineerInvitationResponse::from)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EngineerInvitationResponse> sent(Pageable pageable) {
        return invitationRepository.findByInviterId(currentUser.id(), pageable)
                .map(EngineerInvitationResponse::from)
                .getContent();
    }

    @Override
    @Transactional
    public EngineerInvitationResponse accept(int invitationId) {
        EngineerInvitation invitation = load(invitationId);
        requireEngineer(invitation);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("This invitation is no longer pending");
        }
        if (invitation.getResponseDeadline() != null
                && invitation.getResponseDeadline().isBefore(LocalDate.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation);
            auditService.record("ENGINEER_INVITATION_EXPIRED", "EngineerInvitation", invitation.getId(),
                    "projectId=" + invitation.getProject().getId());
            throw new BadRequestException("This invitation has expired");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitation.setResponseMessage(null);
        invitationRepository.save(invitation);

        ProjectMembership membership = membershipRepository
                .findByProjectIdAndUserId(invitation.getProject().getId(), invitation.getEngineer().getId())
                .orElseGet(ProjectMembership::new);
        membership.setProject(invitation.getProject());
        membership.setUser(invitation.getEngineer());
        membership.setRole(ProjectMemberRole.ENGINEER);
        membership.setPermissions(invitation.getRequestedPermissions());
        membership.setInvitedBy(invitation.getInviter());
        membership.setActive(true);
        membership.setRemovedAt(null);
        if (membership.getCreatedAt() == null) {
            membership.setCreatedAt(LocalDateTime.now());
        }
        ProjectMembership savedMembership = membershipRepository.save(membership);

        auditService.record("ENGINEER_INVITATION_ACCEPTED", "EngineerInvitation", invitation.getId(),
                "projectId=" + invitation.getProject().getId()
                        + "; membershipId=" + savedMembership.getId());
        return EngineerInvitationResponse.from(invitation);
    }

    @Override
    @Transactional
    public EngineerInvitationResponse decline(int invitationId, String responseMessage) {
        EngineerInvitation invitation = load(invitationId);
        requireEngineer(invitation);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("This invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setResponseMessage(clean(responseMessage));
        invitation.setRespondedAt(LocalDateTime.now());
        EngineerInvitation saved = invitationRepository.save(invitation);
        auditService.record("ENGINEER_INVITATION_DECLINED", "EngineerInvitation", saved.getId(),
                "projectId=" + invitation.getProject().getId());
        return EngineerInvitationResponse.from(saved);
    }

    @Override
    @Transactional
    public EngineerInvitationResponse cancel(int invitationId) {
        EngineerInvitation invitation = load(invitationId);
        guard.projectForOwnerOrContractor(invitation.getProject().getId());
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("This invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitation.setRespondedAt(LocalDateTime.now());
        EngineerInvitation saved = invitationRepository.save(invitation);
        auditService.record("ENGINEER_INVITATION_CANCELLED", "EngineerInvitation", saved.getId(),
                "projectId=" + invitation.getProject().getId());
        return EngineerInvitationResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectEngineerResponse> members(int projectId) {
        guard.projectForOwnerOrContractor(projectId);
        return membershipRepository.findByProjectIdAndActiveTrueOrderByIdAsc(projectId).stream()
                .map(m -> {
                    User engineer = m.getUser();
                    EngineerProfile profile = profileRepository.findByUserId(engineer.getId()).orElse(null);
                    return new ProjectEngineerResponse(
                            m.getId(),
                            engineer.getId(),
                            engineer.getName(),
                            engineer.getPhone(),
                            engineer.getEmail(),
                            profile == null ? null : profile.getSpecialty(),
                            profile == null ? 0 : profile.getExperienceYears(),
                            profile == null ? null : profile.getCity(),
                            m.getRole().name(),
                            m.getPermissions(),
                            m.getCreatedAt()
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void removeMember(int projectId, int membershipId) {
        guard.projectForOwnerOrContractor(projectId);
        ProjectMembership membership = membershipRepository.findByIdAndProjectId(membershipId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project engineer not found"));
        if (!membership.isActive()) {
            throw new ResourceNotFoundException("Project engineer not found");
        }

        membership.setActive(false);
        membership.setRemovedAt(LocalDateTime.now());
        membershipRepository.save(membership);
        auditService.record("ENGINEER_REMOVED_FROM_PROJECT", "ProjectMembership", membershipId,
                "projectId=" + projectId + "; userId=" + membership.getUser().getId());
    }

    private EngineerInvitation load(int invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
    }

    private void requireEngineer(EngineerInvitation invitation) {
        if (invitation.getEngineer().getId() != currentUser.id()) {
            throw new AccessDeniedException("This invitation belongs to another engineer");
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

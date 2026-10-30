package com.construction.construction_management.service;

import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.ProjectMembershipRepository;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Central ownership checks. Every service goes through this, so a user can only touch
 * projects and assignments they actually belong to.
 */
@Component
public class AccessGuard {

    private final CurrentUser currentUser;
    private final ProjectRepository projectRepository;
    private final LabourAssignmentRepository assignmentRepository;
    private final ProjectMembershipRepository membershipRepository;

    public AccessGuard(CurrentUser currentUser,
                       ProjectRepository projectRepository,
                       LabourAssignmentRepository assignmentRepository,
                       ProjectMembershipRepository membershipRepository) {
        this.currentUser = currentUser;
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
        this.membershipRepository = membershipRepository;
    }

    private Project load(int projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private boolean isOwner(Project p) {
        return p.getHouseOwner() != null && p.getHouseOwner().getId() == currentUser.id();
    }

    private boolean isContractor(Project p) {
        return p.getContractor() != null && p.getContractor().getId() == currentUser.id();
    }

    /** Owner, contractor, admin, an assigned labourer, or an accepted engineer member. */
    public Project projectForParticipant(int projectId) {
        Project p = load(projectId);
        int userId = currentUser.id();
        if (currentUser.isAdmin() || isOwner(p) || isContractor(p)
                || assignmentRepository.existsByLabourIdAndProjectId(userId, projectId)
                || membershipRepository.existsByProjectIdAndUserIdAndActiveTrue(projectId, userId)) {
            return p;
        }
        throw new AccessDeniedException("No access to this project");
    }

    /** Owner, contractor, admin, or an accepted engineer member (site records). */
    public Project projectForSiteRead(int projectId) {
        Project p = load(projectId);
        int userId = currentUser.id();
        if (currentUser.isAdmin() || isOwner(p) || isContractor(p)
                || membershipRepository.existsByProjectIdAndUserIdAndActiveTrue(projectId, userId)) {
            return p;
        }
        throw new AccessDeniedException("No access to this project's site records");
    }

    /** Contractor or admin (create/edit tasks, milestones and reports on behalf of the site). */
    public Project projectForSiteManagement(int projectId) {
        Project p = load(projectId);
        if (currentUser.isAdmin() || isContractor(p)) {
            return p;
        }
        throw new AccessDeniedException("Only the project contractor or an admin can manage this");
    }

    /** A valid assignee is the owner, contractor, or an active engineer member. */
    public boolean isAssignable(Project project, int userId) {
        return (project.getHouseOwner() != null && project.getHouseOwner().getId() == userId)
                || (project.getContractor() != null && project.getContractor().getId() == userId)
                || membershipRepository.existsByProjectIdAndUserIdAndActiveTrue(project.getId(), userId);
    }

    /** Owner, contractor or admin (money and material details). */
    public Project projectForOwnerOrContractor(int projectId) {
        Project p = load(projectId);
        if (currentUser.isAdmin() || isOwner(p) || isContractor(p)) {
            return p;
        }
        throw new AccessDeniedException("No access to this project");
    }

    public Project projectForContractor(int projectId) {
        Project p = load(projectId);
        if (isContractor(p)) {
            return p;
        }
        throw new AccessDeniedException("Only the project's contractor can do this");
    }

    public Project projectForOwner(int projectId) {
        Project p = load(projectId);
        if (isOwner(p)) {
            return p;
        }
        throw new AccessDeniedException("Only the project's owner can do this");
    }

    private LabourAssignment loadAssignment(int assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    /** Only the contractor of the assignment's project may change it. */
    public LabourAssignment assignmentForContractor(int assignmentId) {
        LabourAssignment a = loadAssignment(assignmentId);
        if (isContractor(a.getProject())) {
            return a;
        }
        throw new AccessDeniedException("Only the project's contractor can do this");
    }

    /** Attendance: contractor, the labourer themself, the project owner, or admin. */
    public LabourAssignment assignmentForAttendance(int assignmentId) {
        LabourAssignment a = loadAssignment(assignmentId);
        if (currentUser.isAdmin() || isContractor(a.getProject()) || isOwner(a.getProject())
                || a.getLabour().getId() == currentUser.id()) {
            return a;
        }
        throw new AccessDeniedException("No access to this assignment");
    }

    /** Wages are private between contractor and labourer (plus admin). */
    public LabourAssignment assignmentForWages(int assignmentId) {
        LabourAssignment a = loadAssignment(assignmentId);
        if (currentUser.isAdmin() || isContractor(a.getProject())
                || a.getLabour().getId() == currentUser.id()) {
            return a;
        }
        throw new AccessDeniedException("No access to this assignment");
    }

    public boolean isRole(RoleName role) {
        return currentUser.role() == role;
    }
}

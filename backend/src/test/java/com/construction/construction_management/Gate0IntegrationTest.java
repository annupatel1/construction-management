package com.construction.construction_management;

import com.construction.construction_management.entity.AccountStatus;
import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.Attendance;
import com.construction.construction_management.entity.AttendanceStatus;
import com.construction.construction_management.entity.ContractType;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectRequirement;
import com.construction.construction_management.entity.ProjectStatus;
import com.construction.construction_management.entity.RequirementStatus;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.entity.AuditEvent;
import com.construction.construction_management.repository.AttendanceRepository;
import com.construction.construction_management.repository.AuditEventRepository;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.repository.ProjectRequirementRepository;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Gate0IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectRequirementRepository requirementRepository;
    @Autowired LabourAssignmentRepository assignmentRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired AuditEventRepository auditEventRepository;
    @Autowired JwtUtil jwtUtil;
    @Autowired EntityManager entityManager;

    private User user(RoleName roleName, String phone, String email) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setName(roleName + " " + phone);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword("not-used-in-these-tests");
        user.setAddress("Indore");
        user.setRole(role);
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.saveAndFlush(user);
    }

    private String token(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getTokenVersion());
    }

    private Project project(User owner, String title) {
        Project project = new Project();
        project.setHouseOwner(owner);
        project.setTitle(title);
        project.setLocation("Indore");
        project.setAreaSqFt(new BigDecimal("1000.00"));
        project.setRatePerSqFt(new BigDecimal("1200.00"));
        project.setTotalEstimatedCost(new BigDecimal("1200000.00"));
        project.setContractType(ContractType.LABOUR_ONLY);
        project.setStatus(ProjectStatus.ONGOING);
        project.setStartDate(LocalDate.now());
        return projectRepository.saveAndFlush(project);
    }

    @Test
    void moneyIsStoredAndReturnedWithTwoDecimals() throws Exception {
        User contractor = user(RoleName.CONTRACTOR, "9000000001", "contractor1@example.com");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Decimal check",
                                  "location": "Indore",
                                  "areaSqFt": 100.555,
                                  "ratePerSqFt": 12.345,
                                  "contractType": "LABOUR_ONLY",
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areaSqFt").value(100.56))
                .andExpect(jsonPath("$.ratePerSqFt").value(12.35))
                // 100.56 * 12.35 = 1241.916, rounded half-up
                .andExpect(jsonPath("$.totalEstimatedCost").value(1241.92));
    }

    @Test
    void creatingAProjectWritesAnAuditEvent() throws Exception {
        User contractor = user(RoleName.CONTRACTOR, "9000000008", "contractor8@example.com");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Audited site",
                                  "location": "Indore",
                                  "areaSqFt": 1000,
                                  "ratePerSqFt": 1200,
                                  "contractType": "LABOUR_ONLY",
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isOk());

        AuditEvent event = auditEventRepository.findAll().stream()
                .filter(e -> e.getAction().equals("PROJECT_CREATED"))
                .findFirst()
                .orElseThrow();
        assertEquals(contractor.getId(), event.getActorId());
        assertEquals("CONTRACTOR", event.getActorRole());
        assertEquals("Project", event.getEntityType());
    }

    @Test
    void requirementResponseDoesNotExposeOwnerEmail() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9000000002", "owner-secret@example.com");
        User contractor = user(RoleName.CONTRACTOR, "9000000003", "contractor2@example.com");

        ProjectRequirement requirement = new ProjectRequirement();
        requirement.setHouseOwner(owner);
        requirement.setCity("Indore");
        requirement.setArea("Vijay Nagar");
        requirement.setPincode("452010");
        requirement.setAreaSqFt(new BigDecimal("1200.00"));
        requirement.setContractType(ContractType.LABOUR_MATERIAL);
        requirement.setDescription("Need a contractor");
        requirement.setStatus(RequirementStatus.OPEN);
        requirement.setPostedDate(LocalDate.now());
        requirementRepository.saveAndFlush(requirement);

        mockMvc.perform(get("/api/requirements")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].houseOwner.email").doesNotExist())
                .andExpect(jsonPath("$[0].houseOwner.password").doesNotExist())
                .andExpect(jsonPath("$[0].houseOwner.phone").value(owner.getPhone()));
    }

    @Test
    void logoutRevokesExistingTokens() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9000000004", "owner4@example.com");
        String bearer = token(owner);

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer))
                .andExpect(status().isOk());

        User stored = userRepository.findById(owner.getId()).orElseThrow();
        stored.setTokenVersion(stored.getTokenVersion() + 1);
        userRepository.saveAndFlush(stored);

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void suspendedAccountCannotUseAnExistingToken() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9000000009", "owner9@example.com");
        String bearer = token(owner);

        User stored = userRepository.findById(owner.getId()).orElseThrow();
        stored.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.saveAndFlush(stored);

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void labourSeesOnlyAssignedProjects() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9000000010", "owner10@example.com");
        User contractor = user(RoleName.CONTRACTOR, "9000000011", "contractor11@example.com");
        User labour = user(RoleName.LABOUR, "9000000012", "labour12@example.com");

        Project project = project(owner, "Assigned site");
        project.setContractor(contractor);
        projectRepository.saveAndFlush(project);

        LabourAssignment assignment = new LabourAssignment();
        assignment.setLabour(labour);
        assignment.setProject(project);
        assignment.setDailyRate(new BigDecimal("600.00"));
        assignment.setAdvanceTaken(BigDecimal.ZERO);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedDate(LocalDate.now());
        assignmentRepository.saveAndFlush(assignment);

        // the same project can be assigned twice; the project list must still show it once
        LabourAssignment second = new LabourAssignment();
        second.setLabour(labour);
        second.setProject(project);
        second.setDailyRate(new BigDecimal("650.00"));
        second.setAdvanceTaken(BigDecimal.ZERO);
        second.setStatus(AssignmentStatus.ASSIGNED);
        second.setAssignedDate(LocalDate.now());
        assignmentRepository.saveAndFlush(second);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", token(labour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Assigned site"));
    }

    @Test
    void projectListIsPaginated() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9000000005", "owner5@example.com");
        project(owner, "Site 1");
        project(owner, "Site 2");
        project(owner, "Site 3");

        mockMvc.perform(get("/api/projects?page=0&size=2")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void attendanceCannotBeDuplicatedForTheSameDay() {
        User owner = user(RoleName.HOUSE_OWNER, "9000000006", "owner6@example.com");
        User labour = user(RoleName.LABOUR, "9000000007", "labour7@example.com");
        Project project = project(owner, "Attendance site");

        LabourAssignment assignment = new LabourAssignment();
        assignment.setLabour(labour);
        assignment.setProject(project);
        assignment.setDailyRate(new BigDecimal("600.00"));
        assignment.setAdvanceTaken(BigDecimal.ZERO);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedDate(LocalDate.now());
        assignmentRepository.saveAndFlush(assignment);

        LocalDate date = LocalDate.now().minusDays(1);
        Attendance first = new Attendance();
        first.setLabourAssignment(assignment);
        first.setDate(date);
        first.setStatus(AttendanceStatus.FULL_DAY);
        first.setOvertimeHours(BigDecimal.ZERO);
        attendanceRepository.saveAndFlush(first);

        Attendance duplicate = new Attendance();
        duplicate.setLabourAssignment(assignment);
        duplicate.setDate(date);
        duplicate.setStatus(AttendanceStatus.HALF_DAY);
        duplicate.setOvertimeHours(BigDecimal.ZERO);

        assertThrows(DataIntegrityViolationException.class,
                () -> attendanceRepository.saveAndFlush(duplicate));
        // the failed insert leaves a half-built entity in the session; clear it before querying
        entityManager.clear();
        assertEquals(1, attendanceRepository.findByLabourAssignmentId(assignment.getId()).size());
    }
}

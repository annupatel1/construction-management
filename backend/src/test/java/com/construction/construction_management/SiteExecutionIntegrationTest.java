package com.construction.construction_management;

import com.construction.construction_management.entity.AccountStatus;
import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.ContractType;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectMemberRole;
import com.construction.construction_management.entity.ProjectMembership;
import com.construction.construction_management.entity.ProjectStatus;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.ProjectMembershipRepository;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SiteExecutionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectMembershipRepository membershipRepository;
    @Autowired LabourAssignmentRepository assignmentRepository;
    @Autowired JwtUtil jwtUtil;

    private final ObjectMapper json = new ObjectMapper();

    private User user(RoleName roleName, String phone) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setName(roleName + " " + phone);
        user.setPhone(phone);
        user.setEmail(phone + "@example.com");
        user.setPassword("not-used-in-these-tests");
        user.setAddress("Indore");
        user.setRole(role);
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.saveAndFlush(user);
    }

    private Project project(User owner, User contractor, String title) {
        Project project = new Project();
        project.setHouseOwner(owner);
        project.setContractor(contractor);
        project.setTitle(title);
        project.setLocation("Indore");
        project.setAreaSqFt(new BigDecimal("1000.00"));
        project.setRatePerSqFt(new BigDecimal("1200.00"));
        project.setTotalEstimatedCost(new BigDecimal("1200000.00"));
        project.setContractType(ContractType.LABOUR_ONLY);
        project.setStatus(ProjectStatus.ONGOING);
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusMonths(6));
        return projectRepository.saveAndFlush(project);
    }

    private void member(Project project, User engineer) {
        ProjectMembership membership = new ProjectMembership();
        membership.setProject(project);
        membership.setUser(engineer);
        membership.setRole(ProjectMemberRole.ENGINEER);
        membership.setPermissions("PROJECT_READ");
        membership.setActive(true);
        membership.setCreatedAt(LocalDateTime.now());
        membershipRepository.saveAndFlush(membership);
    }

    private void labourAssignment(Project project, User labour) {
        LabourAssignment assignment = new LabourAssignment();
        assignment.setProject(project);
        assignment.setLabour(labour);
        assignment.setDailyRate(new BigDecimal("700.00"));
        assignment.setAdvanceTaken(BigDecimal.ZERO);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedDate(LocalDate.now());
        assignmentRepository.saveAndFlush(assignment);
    }

    private String token(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getTokenVersion());
    }

    private int idOf(String response) {
        try {
            return json.readTree(response).get("id").asInt();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    void dailyReportsStayPrivateUntilSubmittedAndPublished() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9200000001");
        User contractor = user(RoleName.CONTRACTOR, "9200000002");
        User engineer = user(RoleName.ENGINEER, "9200000003");
        User outsider = user(RoleName.ENGINEER, "9200000004");
        Project project = project(owner, contractor, "Progress site");
        member(project, engineer);

        String body = """
                {
                  "reportDate": "%s",
                  "plannedWork": "RCC column shuttering",
                  "completedWork": "Columns up to plinth",
                  "blockers": "Steel delivery delayed",
                  "nextDayPlan": "Start beam reinforcement",
                  "photos": [
                    {"photoUrl": "https://example.com/site-1.jpg", "caption": "Plinth columns", "takenDate": "%s"}
                  ]
                }
                """.formatted(LocalDate.now(), LocalDate.now());

        String created = mockMvc.perform(post("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.photos[0].caption").value("Plinth columns"))
                .andReturn().getResponse().getContentAsString();
        int reportId = idOf(created);

        mockMvc.perform(get("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/progress-reports/" + reportId + "/submit")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/projects/" + project.getId() + "/progress-reports/" + reportId + "/publish")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completedWork").value("Columns up to plinth"))
                .andExpect(jsonPath("$[0].photos[0].photoUrl").value("https://example.com/site-1.jpg"));

        mockMvc.perform(put("/api/projects/" + project.getId() + "/progress-reports/" + reportId)
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content(body.replace("Columns up to plinth", "Changed")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tasksMilestonesAndStatusChangesRespectSiteRoles() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9200000011");
        User contractor = user(RoleName.CONTRACTOR, "9200000012");
        User engineer = user(RoleName.ENGINEER, "9200000013");
        User outsider = user(RoleName.ENGINEER, "9200000014");
        Project project = project(owner, contractor, "Task site");
        member(project, engineer);

        String milestone = mockMvc.perform(post("/api/projects/" + project.getId() + "/milestones")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"title":"Plinth complete","targetDate":"%s","status":"IN_PROGRESS"}
                                """.formatted(LocalDate.now().plusDays(14))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn().getResponse().getContentAsString();
        int milestoneId = idOf(milestone);

        mockMvc.perform(post("/api/projects/" + project.getId() + "/milestones")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"title":"Owner cannot add","targetDate":"%s"}
                                """.formatted(LocalDate.now().plusDays(20))))
                .andExpect(status().isForbidden());

        String task = mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Check column reinforcement",
                                  "description": "Inspect spacing and cover before pour",
                                  "milestoneId": %d,
                                  "assigneeId": %d,
                                  "dueDate": "%s"
                                }
                                """.formatted(milestoneId, engineer.getId(), LocalDate.now().minusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(engineer.getId()))
                .andExpect(jsonPath("$.overdue").value(true))
                .andReturn().getResponse().getContentAsString();
        int taskId = idOf(task);

        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"title":"Bad assignee","assigneeId":%d}
                                """.formatted(outsider.getId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/tasks/" + taskId + "/status")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/tasks/" + taskId + "/status")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"status\":\"BLOCKED\",\"delayReason\":\"Waiting for steel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.delayReason").value("Waiting for steel"));

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/tasks/" + taskId + "/status")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.overdue").value(false));

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/tasks/" + taskId + "/status")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("{\"status\":\"TODO\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/milestones")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskCount").value(1))
                .andExpect(jsonPath("$[0].completedTaskCount").value(1));

        mockMvc.perform(delete("/api/projects/" + project.getId() + "/milestones/" + milestoneId)
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].milestoneId").value(nullValue()));
    }

    @Test
    void issuesSupportAssignmentCommentsAndResolutionEvidence() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9200000021");
        User contractor = user(RoleName.CONTRACTOR, "9200000022");
        User engineer = user(RoleName.ENGINEER, "9200000023");
        User outsider = user(RoleName.ENGINEER, "9200000024");
        Project project = project(owner, contractor, "Quality site");
        member(project, engineer);

        String issue = mockMvc.perform(post("/api/projects/" + project.getId() + "/issues")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Honeycombing on column",
                                  "description": "Visible on the north face",
                                  "category": "QUALITY",
                                  "assigneeId": %d,
                                  "dueDate": "%s",
                                  "photos": [{"photoUrl":"https://example.com/issue-1.jpg","caption":"North face"}]
                                }
                                """.formatted(engineer.getId(), LocalDate.now().plusDays(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.raisedBy.id").value(owner.getId()))
                .andExpect(jsonPath("$.photos[0].caption").value("North face"))
                .andReturn().getResponse().getContentAsString();
        int issueId = idOf(issue);

        mockMvc.perform(post("/api/projects/" + project.getId() + "/issues")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"title":"Bad assignee","assigneeId":%d}
                                """.formatted(outsider.getId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/issues/" + issueId)
                        .header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/issues/" + issueId + "/comments")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"message\":\"Will inspect after the pour\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.id").value(engineer.getId()));

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/issues/" + issueId + "/status")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/issues/" + issueId + "/status")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"status\":\"READY_FOR_REVIEW\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/projects/" + project.getId() + "/issues/" + issueId + "/status")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {
                                  "status":"RESOLVED",
                                  "resolutionNote":"Repaired and cured",
                                  "resolutionPhotoUrl":"https://example.com/resolved-1.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/issues/" + issueId)
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.resolutionPhotoUrl").value("https://example.com/resolved-1.jpg"));

        mockMvc.perform(delete("/api/projects/" + project.getId() + "/issues/" + issueId)
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/projects/" + project.getId() + "/issues/" + issueId)
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk());
    }

    @Test
    void labourCannotReadSiteExecutionRecords() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9200000031");
        User contractor = user(RoleName.CONTRACTOR, "9200000032");
        User labour = user(RoleName.LABOUR, "9200000033");
        Project project = project(owner, contractor, "Labour privacy site");
        labourAssignment(project, labour);

        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", token(labour)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/progress-reports")
                        .header("Authorization", token(labour)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", token(labour)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/projects/" + project.getId() + "/milestones")
                        .header("Authorization", token(labour)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/projects/" + project.getId() + "/issues")
                        .header("Authorization", token(labour)))
                .andExpect(status().isForbidden());
    }
}

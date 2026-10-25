package com.construction.construction_management;

import com.construction.construction_management.entity.AccountStatus;
import com.construction.construction_management.entity.ContractType;
import com.construction.construction_management.entity.CredentialStatus;
import com.construction.construction_management.entity.EngineerAvailability;
import com.construction.construction_management.entity.EngineerProfile;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectStatus;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.repository.EngineerProfileRepository;
import com.construction.construction_management.repository.ProjectMembershipRepository;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.JwtUtil;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EngineerMarketplaceIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired EngineerProfileRepository profileRepository;
    @Autowired ProjectMembershipRepository membershipRepository;
    @Autowired JwtUtil jwtUtil;

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

    private EngineerProfile profile(User engineer, String city, String specialty, int experience) {
        EngineerProfile profile = new EngineerProfile();
        profile.setUser(engineer);
        profile.setCity(city);
        profile.setSpecialty(specialty);
        profile.setSkills("RCC, Site supervision");
        profile.setServiceArea("Indore and nearby");
        profile.setLanguages("Hindi, English");
        profile.setExperienceYears(experience);
        profile.setAvailability(EngineerAvailability.AVAILABLE);
        profile.setCredentialStatus(CredentialStatus.UNVERIFIED);
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.saveAndFlush(profile);
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

    private String token(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getTokenVersion());
    }

    @Test
    void directorySearchFiltersAndNeverExposesContactDetails() throws Exception {
        User engineer = user(RoleName.ENGINEER, "9100000001", "engineer1@example.com");
        profile(engineer, "Indore", "Structural", 8);
        User other = user(RoleName.ENGINEER, "9100000002", "engineer2@example.com");
        EngineerProfile otherProfile = profile(other, "Bhopal", "Plumbing", 2);
        otherProfile.setAvailability(EngineerAvailability.BUSY);
        profileRepository.saveAndFlush(otherProfile);
        User owner = user(RoleName.HOUSE_OWNER, "9100000003", "owner1@example.com");

        mockMvc.perform(get("/api/directory/engineers?city=Indore&specialty=Structural")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(engineer.getId()))
                .andExpect(jsonPath("$[0].phone").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].skills[0]").value("RCC"))
                .andExpect(jsonPath("$[0].profileComplete").value(true));

        mockMvc.perform(get("/api/directory/engineers?minExperience=5")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(engineer.getId()));

        mockMvc.perform(get("/api/directory/engineers?availability=AVAILABLE")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(engineer.getId()));
    }

    @Test
    void invitationGrantsProjectAccessOnlyAfterAcceptance() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9100000011", "owner11@example.com");
        User engineer = user(RoleName.ENGINEER, "9100000012", "engineer12@example.com");
        profile(engineer, "Indore", "Site supervision", 6);
        Project project = project(owner, "Invitation site");

        String invitationBody = """
                {
                  "engineerId": %d,
                  "scope": "Site supervision and quality checks",
                  "startDate": "2026-11-01",
                  "endDate": "2026-12-31",
                  "feeTerms": "Rs 40,000 per month",
                  "responseDeadline": "%s"
                }
                """.formatted(engineer.getId(), LocalDate.now().plusMonths(2));

        String response = mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content(invitationBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.engineer.id").value(engineer.getId()))
                .andExpect(jsonPath("$.engineer.phone").doesNotExist())
                .andExpect(jsonPath("$.inviter.phone").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        int invitationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asInt();

        // Pending invitation must not leak the project.
        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/engineer-invitations/inbox")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/accept")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Invitation site"))
                .andExpect(jsonPath("$.ratePerSqFt").value(nullValue()))
                .andExpect(jsonPath("$.totalEstimatedCost").value(nullValue()));

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Accepted membership is site-read-only: financial and wage endpoints stay closed.
        mockMvc.perform(get("/api/payments/project/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/expenses/project/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/assignments/project/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/projects/" + project.getId() + "/engineers")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/projects/" + project.getId() + "/engineers")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].phone").value(engineer.getPhone()));

        int membershipId = membershipRepository.findByProjectIdAndUserId(project.getId(), engineer.getId())
                .orElseThrow().getId();
        mockMvc.perform(delete("/api/projects/" + project.getId() + "/engineers/" + membershipId)
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyTheInvitedEngineerCanRespondAndDeclineCreatesNoMembership() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9100000021", "owner21@example.com");
        User engineer = user(RoleName.ENGINEER, "9100000022", "engineer22@example.com");
        User otherEngineer = user(RoleName.ENGINEER, "9100000023", "engineer23@example.com");
        profile(engineer, "Indore", "Structural", 10);
        profile(otherEngineer, "Indore", "Structural", 10);
        Project project = project(owner, "Decline site");

        String response = mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"engineerId": %d, "scope": "Review structural drawings", "startDate": "2026-11-01", "endDate": "2026-12-01"}
                                """.formatted(engineer.getId())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int invitationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asInt();

        mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"engineerId": %d, "scope": "No dates supplied"}
                                """.formatted(otherEngineer.getId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"engineerId": %d, "scope": "Attempted admin grant", "requestedPermissions": "PROJECT_ADMIN", "startDate": "2026-11-01", "endDate": "2026-12-01"}
                                """.formatted(otherEngineer.getId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/accept")
                        .header("Authorization", token(otherEngineer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/decline")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{\"responseMessage\":\"Already committed elsewhere\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.responseMessage").value("Already committed elsewhere"));

        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyProjectOwnerOrContractorCanInviteAndCancel() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9100000041", "owner41@example.com");
        User otherOwner = user(RoleName.HOUSE_OWNER, "9100000042", "owner42@example.com");
        User engineer = user(RoleName.ENGINEER, "9100000043", "engineer43@example.com");
        profile(engineer, "Indore", "Quality", 4);
        Project project = project(owner, "Authorization site");

        String body = """
                {"engineerId": %d, "scope": "Quality checks", "startDate": "2026-11-01", "endDate": "2026-12-01"}
                """.formatted(engineer.getId());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(otherOwner))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());

        String response = mockMvc.perform(post("/api/projects/" + project.getId() + "/engineer-invitations")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int invitationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asInt();

        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/cancel")
                        .header("Authorization", token(otherOwner)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/cancel")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/accept")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void engineerProfileIsEditableOnlyByEngineersAndValidated() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9100000031", "owner31@example.com");
        User engineer = user(RoleName.ENGINEER, "9100000032", "engineer32@example.com");

        mockMvc.perform(put("/api/engineers/me")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"specialty":"Structural","experienceYears":5,"city":"Indore","availability":"AVAILABLE"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/engineers/me")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"specialty":"Structural","city":"Indore","availability":"AVAILABLE"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/engineers/me")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {
                                  "specialty":"Structural",
                                  "skills":"RCC, Site supervision",
                                  "experienceYears":5,
                                  "city":"Indore",
                                  "serviceArea":"Indore",
                                  "languages":"Hindi, English",
                                  "availability":"AVAILABLE",
                                  "bio":"Residential structural engineer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value(engineer.getPhone()))
                .andExpect(jsonPath("$.profileComplete").value(true));

        mockMvc.perform(get("/api/engineers/" + engineer.getId())
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());
    }
}

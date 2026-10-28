package com.construction.construction_management;

import com.construction.construction_management.entity.AccountStatus;
import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.Attendance;
import com.construction.construction_management.entity.AttendanceStatus;
import com.construction.construction_management.entity.ContractType;
import com.construction.construction_management.entity.CredentialStatus;
import com.construction.construction_management.entity.EngineerAvailability;
import com.construction.construction_management.entity.EngineerProfile;
import com.construction.construction_management.entity.Expense;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.LabourPayment;
import com.construction.construction_management.entity.LabourPaymentType;
import com.construction.construction_management.entity.MaterialCategory;
import com.construction.construction_management.entity.MaterialItem;
import com.construction.construction_management.entity.Notification;
import com.construction.construction_management.entity.Payment;
import com.construction.construction_management.entity.PaymentMode;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectStatus;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.StockMovement;
import com.construction.construction_management.entity.StockMovementType;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.repository.AttendanceRepository;
import com.construction.construction_management.repository.EngineerProfileRepository;
import com.construction.construction_management.repository.ExpenseRepository;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.LabourPaymentRepository;
import com.construction.construction_management.repository.MaterialItemRepository;
import com.construction.construction_management.repository.NotificationRepository;
import com.construction.construction_management.repository.PaymentRepository;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.StockMovementRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PilotHardeningIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired EngineerProfileRepository profileRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired ExpenseRepository expenseRepository;
    @Autowired LabourAssignmentRepository assignmentRepository;
    @Autowired LabourPaymentRepository labourPaymentRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired MaterialItemRepository materialRepository;
    @Autowired StockMovementRepository movementRepository;
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

    private Project project(User owner, User contractor, String title) {
        Project project = new Project();
        project.setHouseOwner(owner);
        project.setContractor(contractor);
        project.setTitle(title);
        project.setLocation("Indore");
        project.setAreaSqFt(new BigDecimal("1000.00"));
        project.setRatePerSqFt(new BigDecimal("1200.00"));
        project.setTotalEstimatedCost(new BigDecimal("1200000.00"));
        project.setContractType(ContractType.LABOUR_MATERIAL);
        project.setStatus(ProjectStatus.ONGOING);
        project.setStartDate(LocalDate.now());
        return projectRepository.saveAndFlush(project);
    }

    private EngineerProfile profile(User engineer) {
        EngineerProfile profile = new EngineerProfile();
        profile.setUser(engineer);
        profile.setCity("Indore");
        profile.setSpecialty("Site supervision");
        profile.setSkills("RCC, Quality checks");
        profile.setServiceArea("Indore");
        profile.setLanguages("Hindi, English");
        profile.setExperienceYears(5);
        profile.setAvailability(EngineerAvailability.AVAILABLE);
        profile.setCredentialStatus(CredentialStatus.UNVERIFIED);
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.saveAndFlush(profile);
    }

    private String token(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getTokenVersion());
    }

    @Test
    void healthAndErrorsExposeARequestId() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"));

        mockMvc.perform(get("/api/projects")
                        .header("X-Request-Id", "pilot-request-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-Id", "pilot-request-123"))
                .andExpect(jsonPath("$.requestId").value("pilot-request-123"));
    }

    @Test
    void notificationsArePersonalAndCanBeMarkedRead() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9200000001", "pilot-owner1@example.com");
        User contractor = user(RoleName.CONTRACTOR, "9200000002", "pilot-contractor1@example.com");
        User engineer = user(RoleName.ENGINEER, "9200000003", "pilot-engineer1@example.com");
        profile(engineer);
        Project project = project(owner, contractor, "Notification site");

        String invitationBody = """
                {
                  "engineerId": %d,
                  "scope": "Supervise structure and finishing",
                  "startDate": "%s",
                  "endDate": "%s"
                }
                """.formatted(engineer.getId(), LocalDate.now(), LocalDate.now().plusMonths(2));

        String invitationResponse = mockMvc.perform(
                        post("/api/projects/" + project.getId() + "/engineer-invitations")
                                .header("Authorization", token(owner))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invitationBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int invitationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(invitationResponse).get("id").asInt();

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("New project invitation"))
                .andExpect(jsonPath("$[0].link").value("/invitations"));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        // Another user cannot see or mark someone else's notification.
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Notification invitationNotification = notificationRepository.findAll().stream()
                .filter(notification -> notification.getRecipient().getId() == engineer.getId())
                .findFirst().orElseThrow();
        mockMvc.perform(post("/api/notifications/" + invitationNotification.getId() + "/read")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/notifications/" + invitationNotification.getId() + "/read")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        // Accepting the invitation notifies the inviter.
        mockMvc.perform(post("/api/engineer-invitations/" + invitationId + "/accept")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Invitation accepted"))
                .andExpect(jsonPath("$[0].link").value("/projects/" + project.getId()));
    }

    @Test
    void projectExportsContainDataAndRespectRoleBoundaries() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9200000011", "pilot-owner11@example.com");
        User contractor = user(RoleName.CONTRACTOR, "9200000012", "pilot-contractor12@example.com");
        User engineer = user(RoleName.ENGINEER, "9200000013", "pilot-engineer13@example.com");
        User labour = user(RoleName.LABOUR, "9200000014", "pilot-labour14@example.com");
        Project project = project(owner, contractor, "Export site");

        Payment payment = new Payment();
        payment.setProject(project);
        payment.setAmount(new BigDecimal("5000.00"));
        payment.setPaymentDate(LocalDate.now());
        payment.setMode(PaymentMode.CASH);
        payment.setRemarks("Advance payment");
        paymentRepository.saveAndFlush(payment);

        Expense expense = new Expense();
        expense.setProject(project);
        expense.setCategory(MaterialCategory.CEMENT);
        expense.setAmount(new BigDecimal("1000.00"));
        expense.setQuantity(new BigDecimal("10.000"));
        expense.setUnit("bags");
        expense.setPurchaseDate(LocalDate.now());
        expenseRepository.saveAndFlush(expense);

        LabourAssignment assignment = new LabourAssignment();
        assignment.setProject(project);
        assignment.setLabour(labour);
        assignment.setDailyRate(new BigDecimal("600.00"));
        assignment.setAdvanceTaken(BigDecimal.ZERO);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedDate(LocalDate.now());
        assignmentRepository.saveAndFlush(assignment);

        Attendance attendance = new Attendance();
        attendance.setLabourAssignment(assignment);
        attendance.setDate(LocalDate.now());
        attendance.setStatus(AttendanceStatus.FULL_DAY);
        attendance.setOvertimeHours(BigDecimal.ZERO);
        attendanceRepository.saveAndFlush(attendance);

        LabourPayment labourPayment = new LabourPayment();
        labourPayment.setLabourAssignment(assignment);
        labourPayment.setAmount(new BigDecimal("300.00"));
        labourPayment.setPaymentDate(LocalDate.now());
        labourPayment.setMode(PaymentMode.CASH);
        labourPayment.setType(LabourPaymentType.WAGE);
        labourPayment.setRemarks("Weekly wage");
        labourPaymentRepository.saveAndFlush(labourPayment);

        MaterialItem material = new MaterialItem();
        material.setProject(project);
        material.setName("Cement");
        material.setCategory(MaterialCategory.CEMENT);
        material.setUnit("bags");
        material.setMinimumStock(new BigDecimal("10.000"));
        material.setActive(true);
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        materialRepository.saveAndFlush(material);

        StockMovement movement = new StockMovement();
        movement.setProject(project);
        movement.setMaterial(material);
        movement.setMovementType(StockMovementType.RECEIPT);
        movement.setQuantity(new BigDecimal("50.000"));
        movement.setUnitCost(new BigDecimal("380.00"));
        movement.setMovementDate(LocalDate.now());
        movement.setReferenceType("PURCHASE_ORDER");
        movement.setNotes("Opening stock");
        movement.setCreatedBy(contractor);
        movement.setCreatedAt(LocalDateTime.now());
        movementRepository.saveAndFlush(movement);

        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/finance.csv")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("finance-project-" + project.getId() + ".csv")))
                .andExpect(content().string(containsString("Owner payment")))
                .andExpect(content().string(containsString("Advance payment")))
                .andExpect(content().string(containsString("5000.00")))
                .andExpect(content().string(containsString("Material expenses")))
                .andExpect(content().string(containsString("1000.00")))
                .andExpect(content().string(not(containsString("Labour payment"))));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/finance.csv")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Labour payment")))
                .andExpect(content().string(containsString("WAGE")))
                .andExpect(content().string(containsString("Weekly wage")))
                .andExpect(content().string(containsString("300.00")));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/attendance.csv")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LABOUR 9200000014")))
                .andExpect(content().string(containsString("600.00")))
                .andExpect(content().string(containsString("FULL_DAY")));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/stock.csv")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cement")))
                .andExpect(content().string(containsString("50.000")))
                .andExpect(content().string(containsString("Opening stock")));

        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/site.csv")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Site operations export")));

        // Money and stock exports are closed to engineers; attendance export is contractor/admin only.
        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/finance.csv")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/stock.csv")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/projects/" + project.getId() + "/exports/attendance.csv")
                        .header("Authorization", token(owner)))
                .andExpect(status().isForbidden());

        // Controller errors handled by @RestControllerAdvice also echo the request id.
        mockMvc.perform(get("/api/projects/not-a-number")
                        .header("Authorization", token(owner))
                        .header("X-Request-Id", "validation-request-123"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "validation-request-123"))
                .andExpect(jsonPath("$.requestId").value("validation-request-123"));
    }
}

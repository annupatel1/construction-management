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
class MaterialsIntegrationTest {

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
        project.setContractType(ContractType.LABOUR_MATERIAL);
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
    void procurementReceiptsStockAndPermissionsStayConsistent() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9300000001");
        User contractor = user(RoleName.CONTRACTOR, "9300000002");
        User engineer = user(RoleName.ENGINEER, "9300000003");
        User outsider = user(RoleName.ENGINEER, "9300000004");
        User labour = user(RoleName.LABOUR, "9300000005");
        Project project = project(owner, contractor, "Materials site");
        member(project, engineer);
        labourAssignment(project, labour);
        String base = "/api/projects/" + project.getId();

        String material = mockMvc.perform(post(base + "/materials")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"name":"OPC Cement","category":"CEMENT","unit":"bags","minimumStock":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.lowStock").value(true))
                .andReturn().getResponse().getContentAsString();
        int materialId = idOf(material);

        mockMvc.perform(post(base + "/materials")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"name":"OPC Cement","category":"CEMENT","unit":"bags","minimumStock":100}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(base + "/materials")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"name":"Steel","category":"STEEL","unit":"kg"}
                                """))
                .andExpect(status().isForbidden());

        String supplier = mockMvc.perform(post(base + "/suppliers")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"name":"Shree Cement Depot","phone":"9876543210","address":"Indore"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int supplierId = idOf(supplier);

        String request = mockMvc.perform(post(base + "/material-requests")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {
                                  "materialId": %d,
                                  "quantity": 100,
                                  "neededBy": "%s",
                                  "notes": "For slab pour"
                                }
                                """.formatted(materialId, LocalDate.now().plusDays(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.materialName").value("OPC Cement"))
                .andReturn().getResponse().getContentAsString();
        int requestId = idOf(request);

        mockMvc.perform(post(base + "/material-requests/" + requestId + "/approve")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(base + "/material-requests/" + requestId + "/approve")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("{\"reviewNote\":\"Approved for slab\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewNote").value("Approved for slab"));

        mockMvc.perform(post(base + "/purchase-orders")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"materialRequestId":%d,"materialId":%d,"supplierId":%d,
                                 "quantity":100,"unitPrice":450.50}
                                """.formatted(requestId, materialId, supplierId)))
                .andExpect(status().isForbidden());

        String order = mockMvc.perform(post(base + "/purchase-orders")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {
                                  "materialRequestId": %d,
                                  "materialId": %d,
                                  "supplierId": %d,
                                  "quantity": 100,
                                  "unitPrice": 450.50,
                                  "expectedDate": "%s",
                                  "notes": "First load"
                                }
                                """.formatted(requestId, materialId, supplierId,
                                LocalDate.now().plusDays(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andExpect(jsonPath("$.totalAmount").value(45050.0))
                .andReturn().getResponse().getContentAsString();
        int orderId = idOf(order);

        mockMvc.perform(post(base + "/purchase-orders/" + orderId + "/receive")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"receivedQuantity":40,"receivedDate":"%s","invoiceNumber":"INV-1"}
                                """.formatted(LocalDate.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.receivedQuantity").value(40.0))
                .andExpect(jsonPath("$.receipts.length()").value(1));

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials[0].balance").value(40.0))
                .andExpect(jsonPath("$.materials[0].lowStock").value(true))
                .andExpect(jsonPath("$.lowStockCount").value(1))
                .andExpect(jsonPath("$.movements[0].movementType").value("RECEIPT"));

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movements[0].unitCost").value(nullValue()));

        mockMvc.perform(get(base + "/suppliers")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(base + "/purchase-orders")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(base + "/purchase-orders")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAmount").value(45050.0));

        mockMvc.perform(get("/api/expenses/project/" + project.getId())
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amount").value(18020.0))
                .andExpect(jsonPath("$[0].quantity").value(40.0));

        mockMvc.perform(post(base + "/purchase-orders/" + orderId + "/receive")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"receivedQuantity":60,"receivedDate":"%s","invoiceNumber":"INV-2"}
                                """.formatted(LocalDate.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.receivedQuantity").value(100.0))
                .andExpect(jsonPath("$.remainingQuantity").value(0.0))
                .andExpect(jsonPath("$.receipts.length()").value(2));

        mockMvc.perform(post(base + "/purchase-orders/" + orderId + "/cancel")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(base + "/material-requests")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("FULFILLED"));

        String retryRequest = mockMvc.perform(post(base + "/material-requests")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"materialId":%d,"quantity":5,"notes":"Retry after cancellation"}
                                """.formatted(materialId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int retryRequestId = idOf(retryRequest);

        mockMvc.perform(post(base + "/material-requests/" + retryRequestId + "/approve")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        String retryOrder = mockMvc.perform(post(base + "/purchase-orders")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"materialRequestId":%d,"materialId":%d,"supplierId":%d,
                                 "quantity":5,"unitPrice":450.50}
                                """.formatted(retryRequestId, materialId, supplierId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int retryOrderId = idOf(retryOrder);

        mockMvc.perform(post(base + "/purchase-orders/" + retryOrderId + "/cancel")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get(base + "/material-requests")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        String reordered = mockMvc.perform(post(base + "/purchase-orders")
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"materialRequestId":%d,"materialId":%d,"supplierId":%d,
                                 "quantity":5,"unitPrice":460}
                                """.formatted(retryRequestId, materialId, supplierId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andReturn().getResponse().getContentAsString();
        int reorderedId = idOf(reordered);

        mockMvc.perform(post(base + "/material-requests/" + retryRequestId + "/cancel")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(base + "/purchase-orders/" + reorderedId + "/cancel")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post(base + "/material-requests/" + retryRequestId + "/cancel")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post(base + "/stock-movements")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"materialId":%d,"movementType":"ISSUE","quantity":30,"notes":"Slab pour"}
                                """.formatted(materialId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movementType").value("ISSUE"));

        mockMvc.perform(post(base + "/stock-movements")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"materialId":%d,"movementType":"ISSUE","quantity":80}
                                """.formatted(materialId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(base + "/stock-movements")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"materialId":%d,"movementType":"WASTAGE","quantity":5}
                                """.formatted(materialId)))
                .andExpect(status().isOk());

        mockMvc.perform(post(base + "/stock-movements")
                        .header("Authorization", token(engineer))
                        .contentType("application/json")
                        .content("""
                                {"materialId":%d,"movementType":"RETURN","quantity":10}
                                """.formatted(materialId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials[0].balance").value(75.0))
                .andExpect(jsonPath("$.materials[0].lowStock").value(true))
                .andExpect(jsonPath("$.movements.length()").value(5));

        mockMvc.perform(post(base + "/stock-movements")
                        .header("Authorization", token(owner))
                        .contentType("application/json")
                        .content("""
                                {"materialId":%d,"movementType":"ISSUE","quantity":1}
                                """.formatted(materialId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(labour)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put(base + "/materials/" + materialId)
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"name":"OPC Cement","category":"CEMENT","unit":"kg","minimumStock":100}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put(base + "/materials/" + materialId)
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"name":"OPC Cement","category":"CEMENT","unit":"bags","minimumStock":100,"active":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials[0].active").value(false))
                .andExpect(jsonPath("$.lowStockCount").value(0));

        mockMvc.perform(get(base + "/stock-ledger")
                        .header("Authorization", token(engineer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials.length()").value(0));

        mockMvc.perform(put(base + "/materials/" + materialId)
                        .header("Authorization", token(contractor))
                        .contentType("application/json")
                        .content("""
                                {"name":"OPC Cement","category":"CEMENT","unit":"bags","minimumStock":100,"active":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}

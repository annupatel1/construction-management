package com.construction.construction_management;

import com.construction.construction_management.entity.AccountStatus;
import com.construction.construction_management.entity.ContractType;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectStatus;
import com.construction.construction_management.entity.Role;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.repository.RoleRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "app.uploads.dir=target/test-uploads",
        "app.uploads.max-bytes=2048"
})
class FileUploadIntegrationTest {

    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Z0ZkAAAAASUVORK5CYII=");
    private static final Path UPLOAD_DIR = Path.of("target/test-uploads");

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired JwtUtil jwtUtil;

    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void cleanUploads() throws IOException {
        if (!Files.isDirectory(UPLOAD_DIR)) {
            return;
        }
        try (Stream<Path> paths = Files.list(UPLOAD_DIR)) {
            for (Path path : paths.toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

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

    private Project project(User owner, User contractor) {
        Project project = new Project();
        project.setHouseOwner(owner);
        project.setContractor(contractor);
        project.setTitle("Photo upload site");
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

    private String token(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getTokenVersion());
    }

    @Test
    void uploadsAndServesAPhoto() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9300000001");
        User contractor = user(RoleName.CONTRACTOR, "9300000002");
        Project project = project(owner, contractor);

        String body = mockMvc.perform(multipart("/api/projects/" + project.getId() + "/files")
                        .file(new MockMultipartFile("file", "site.png", "image/png", PNG))
                        .header("Authorization", token(owner)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value(startsWith("project-" + project.getId() + "-")))
                .andExpect(jsonPath("$.url").value(containsString("/api/files/")))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.size").value(PNG.length))
                .andReturn().getResponse().getContentAsString();

        String path = URI.create(json.readTree(body).get("url").asText()).getPath();
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; sandbox"))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void rejectsUsersWithoutProjectAccess() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9300000011");
        User contractor = user(RoleName.CONTRACTOR, "9300000012");
        User outsider = user(RoleName.ENGINEER, "9300000013");
        Project project = project(owner, contractor);

        mockMvc.perform(multipart("/api/projects/" + project.getId() + "/files")
                        .file(new MockMultipartFile("file", "site.png", "image/png", PNG))
                        .header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsNonImageFiles() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9300000021");
        User contractor = user(RoleName.CONTRACTOR, "9300000022");
        Project project = project(owner, contractor);

        mockMvc.perform(multipart("/api/projects/" + project.getId() + "/files")
                        .file(new MockMultipartFile("file", "notes.txt", "text/plain",
                                "not an image".getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", token(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only JPG, PNG or WebP images are supported"));
    }

    @Test
    void rejectsAFileWhoseDeclaredTypeDoesNotMatchItsBytes() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9300000031");
        User contractor = user(RoleName.CONTRACTOR, "9300000032");
        Project project = project(owner, contractor);

        mockMvc.perform(multipart("/api/projects/" + project.getId() + "/files")
                        .file(new MockMultipartFile("file", "site.jpg", "image/jpeg", PNG))
                        .header("Authorization", token(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The file type does not match the image content"));
    }

    @Test
    void rejectsOversizedPhotos() throws Exception {
        User owner = user(RoleName.HOUSE_OWNER, "9300000041");
        User contractor = user(RoleName.CONTRACTOR, "9300000042");
        Project project = project(owner, contractor);

        mockMvc.perform(multipart("/api/projects/" + project.getId() + "/files")
                        .file(new MockMultipartFile("file", "big.png", "image/png", new byte[3000]))
                        .header("Authorization", token(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Photo is too large")));
    }
}

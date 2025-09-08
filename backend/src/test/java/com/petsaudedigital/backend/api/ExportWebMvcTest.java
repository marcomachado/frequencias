package com.petsaudedigital.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsaudedigital.backend.api.dto.ExportDtos;
import com.petsaudedigital.backend.domain.*;
import com.petsaudedigital.backend.domain.enums.ActivityType;
import com.petsaudedigital.backend.domain.enums.AttendanceMode;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import com.petsaudedigital.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportWebMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ProjectRepository projectRepository;
    @Autowired AxisRepository axisRepository;
    @Autowired GtRepository gtRepository;
    @Autowired UserRepository userRepository;
    @Autowired ActivityRepository activityRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired ExportLogRepository exportLogRepository;
    @Autowired UserRoleRepository userRoleRepository;

    Long gtId; Long userId;

    @BeforeEach
    void setup() {
        attendanceRepository.deleteAll();
        activityRepository.deleteAll();
        gtRepository.deleteAll();
        axisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        exportLogRepository.deleteAll();

        User u = new User(); u.setNome("Maria"); u.setEmail("maria@example.com"); u.setPasswordHash("x"); userRepository.save(u); userId = u.getId();
        UserRole ur = new UserRole(); ur.setUser(u); ur.setId(new UserRole.Id(userId, "coordenador_geral")); userRoleRepository.save(ur);
        Project p = new Project(); p.setNome("P"); p.setDescricao("D"); p.setAtivo(1); p.setCreatedAt("2025-09-07T00:00:00"); projectRepository.save(p);
        Axis a = new Axis(); a.setProject(p); a.setNome("Eixo"); a.setAtivo(1); axisRepository.save(a);
        Gt gt = new Gt(); gt.setProject(p); gt.setAxis(a); gt.setNome("GT"); gt.setAtivo(1); gtRepository.save(gt); gtId = gt.getId();

        Activity act = new Activity();
        act.setProject(p); act.setGt(gt); act.setTipo(ActivityType.individual);
        act.setTitulo("A"); act.setData("2025-09-05"); act.setInicio("09:00"); act.setFim("10:00"); act.setCreatedBy(u);
        activityRepository.save(act);
        Attendance att = new Attendance(); att.setActivity(act); att.setUser(u); att.setStatus(AttendanceStatus.validada); att.setModo(AttendanceMode.manual); att.setCreatedAt("2025-09-05T10:00:00");
        attendanceRepository.save(att);
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void export_json_creates_log_and_returns_structure() throws Exception {
        ExportDtos.Request req = new ExportDtos.Request("GT", gtId, "2025-09-01", "2025-09-30", java.util.List.of(gtId), "1.2.0");
        String res = mvc.perform(post("/api/v1/exports/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = om.readTree(res);
        assertThat(node.get("meta").get("schema_version").asText()).isEqualTo("1.2.0");
        assertThat(node.get("eixos").toString()).contains("GT");
        assertThat(exportLogRepository.count()).isEqualTo(1);
    }
}

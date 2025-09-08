package com.petsaudedigital.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(username = "admin@example.com", roles = {"coordenador_geral"})
class TimesheetReportsWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired ProjectRepository projectRepository;
    @Autowired AxisRepository axisRepository;
    @Autowired GtRepository gtRepository;
    @Autowired UserRepository userRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired ActivityRepository activityRepository;

    Long gtId; Long userId;

    @BeforeEach
    void setup() {
        attendanceRepository.deleteAll();
        activityRepository.deleteAll();
        gtRepository.deleteAll();
        axisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User u = new User();
        u.setNome("Aluno");
        u.setEmail("aluno@example.com");
        u.setPasswordHash("x");
        userRepository.save(u);
        userId = u.getId();

        Project p = new Project();
        p.setNome("P"); p.setDescricao("D"); p.setAtivo(1); p.setCreatedAt("2025-09-07T00:00:00");
        projectRepository.save(p);
        Axis a = new Axis(); a.setProject(p); a.setNome("Eixo"); a.setAtivo(1); axisRepository.save(a);
        Gt gt = new Gt(); gt.setProject(p); gt.setAxis(a); gt.setNome("GT"); gt.setAtivo(1); gtRepository.save(gt); gtId = gt.getId();

        Activity act1 = new Activity();
        act1.setProject(p); act1.setGt(gt); act1.setTipo(ActivityType.individual);
        act1.setTitulo("A1"); act1.setData("2025-09-05"); act1.setInicio("09:00"); act1.setFim("11:00"); act1.setCreatedBy(u);
        activityRepository.save(act1);
        Attendance att1 = new Attendance(); att1.setActivity(act1); att1.setUser(u); att1.setStatus(AttendanceStatus.validada); att1.setModo(AttendanceMode.manual); att1.setCreatedAt("2025-09-05T11:00:00");
        attendanceRepository.save(att1);

        Activity act2 = new Activity();
        act2.setProject(p); act2.setGt(gt); act2.setTipo(ActivityType.individual);
        act2.setTitulo("A2"); act2.setData("2025-09-10"); act2.setInicio("10:00"); act2.setFim("12:30"); act2.setCreatedBy(u);
        activityRepository.save(act2);
        Attendance att2 = new Attendance(); att2.setActivity(act2); att2.setUser(u); att2.setStatus(AttendanceStatus.validada); att2.setModo(AttendanceMode.manual); att2.setCreatedAt("2025-09-10T12:30:00");
        attendanceRepository.save(att2);

        Activity act3 = new Activity();
        act3.setProject(p); act3.setGt(gt); act3.setTipo(ActivityType.individual);
        act3.setTitulo("A3"); act3.setData("2025-09-12"); act3.setInicio("08:00"); act3.setFim("09:00"); act3.setCreatedBy(u);
        activityRepository.save(act3);
        Attendance att3 = new Attendance(); att3.setActivity(act3); att3.setUser(u); att3.setStatus(AttendanceStatus.pendente); att3.setModo(AttendanceMode.manual); att3.setCreatedAt("2025-09-12T09:00:00");
        attendanceRepository.save(att3);
    }

    @Test
    void timesheet_summary_and_pdf() throws Exception {
        String json = mvc.perform(get("/api/v1/users/" + userId + "/timesheet")
                        .param("from", "2025-09-01").param("to", "2025-09-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = om.readTree(json);
        double horas = node.get("totalHoras").asDouble();
        // 2h + 2.5h = 4.5h
        assertThat(horas).isEqualTo(4.5);

        mvc.perform(post("/api/v1/users/" + userId + "/timesheet/pdf")
                        .param("from", "2025-09-01").param("to", "2025-09-30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void reports_hours_by_user_and_pending() throws Exception {
        String hrs = mvc.perform(get("/api/v1/reports/hours-by-user")
                        .param("gtId", String.valueOf(gtId)).param("month", "2025-09"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(hrs).contains("aluno@example.com");

        String pend = mvc.perform(get("/api/v1/reports/pending-attendances")
                        .param("gtId", String.valueOf(gtId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(pend).contains("A3"); // response carries activity id; at least ensure presence exists
    }
}

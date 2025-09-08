package com.petsaudedigital.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsaudedigital.backend.domain.*;
import com.petsaudedigital.backend.domain.enums.ActivityType;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActivitiesWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired ProjectRepository projectRepository;
    @Autowired AxisRepository axisRepository;
    @Autowired GtRepository gtRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserGtRepository userGtRepository;
    @Autowired UserRoleRepository userRoleRepository;

    Long gtId;
    Long userId;

    @BeforeEach
    void setup() {
        userGtRepository.deleteAll();
        gtRepository.deleteAll();
        axisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User u = new User();
        u.setNome("Maria");
        u.setEmail("maria@example.com");
        u.setPasswordHash("x");
        userRepository.save(u);
        userId = u.getId();
        // concede papel coordenador_geral para permitir escopo via @scope
        UserRole ur = new UserRole();
        ur.setUser(u);
        ur.setId(new UserRole.Id(userId, "coordenador_geral"));
        userRoleRepository.save(ur);

        Project p = new Project();
        p.setNome("Projeto");
        p.setDescricao("D");
        p.setAtivo(1);
        p.setCreatedAt("2025-09-07T00:00:00");
        projectRepository.save(p);

        Axis a = new Axis();
        a.setProject(p);
        a.setNome("Eixo");
        a.setAtivo(1);
        axisRepository.save(a);

        Gt gt = new Gt();
        gt.setProject(p);
        gt.setAxis(a);
        gt.setNome("GT");
        gt.setAtivo(1);
        gtRepository.save(gt);
        gtId = gt.getId();

        // Maria é membro do GT
        UserGt ug = new UserGt();
        ug.setId(new UserGt.Id(userId, gtId));
        ug.setUser(u);
        ug.setGt(gt);
        ug.setRoleInGt("monitor");
        userGtRepository.save(ug);
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void collective_all_gt_auto_attendance() throws Exception {
        String payload = "{\n" +
                "  \"tipo\": \"coletiva\",\n" +
                "  \"titulo\": \"Reunião\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"10:00\",\n" +
                "  \"fim\": \"12:00\",\n" +
                "  \"target\": { \"type\": \"ALL_GT\" }\n" +
                "}";

        String res = mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = om.readTree(res);
        Long activityId = json.get("id").asLong();

        String attList = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(attList).contains("validada");
    }

    @Test
    @WithMockUser(username = "aluno@example.com", roles = {"monitor"})
    void individual_activity_from_non_privileged_is_pending() throws Exception {
        // criar usuário do token
        User u = new User();
        u.setNome("Aluno");
        u.setEmail("aluno@example.com");
        u.setPasswordHash("x");
        userRepository.save(u);

        String payload = "{\n" +
                "  \"tipo\": \"individual\",\n" +
                "  \"titulo\": \"Estudo\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"08:00\",\n" +
                "  \"fim\": \"09:00\"\n" +
                "}";

        String res = mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long activityId = om.readTree(res).get("id").asLong();
        String attList = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(attList).contains("pendente");
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void collective_by_role_targets_only_matching_members() throws Exception {
        // adiciona outro membro com role diferente
        User p = new User();
        p.setNome("Paulo");
        p.setEmail("paulo@example.com");
        p.setPasswordHash("x");
        userRepository.save(p);

        // vincula Paulo ao GT como preceptor
        UserGt ug2 = new UserGt();
        ug2.setId(new UserGt.Id(p.getId(), gtId));
        ug2.setUser(p);
        ug2.setGt(gtRepository.findById(gtId).orElseThrow());
        ug2.setRoleInGt("preceptor");
        userGtRepository.save(ug2);

        String payload = "{\n" +
                "  \"tipo\": \"coletiva\",\n" +
                "  \"titulo\": \"Treino\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"09:00\",\n" +
                "  \"fim\": \"10:00\",\n" +
                "  \"target\": { \"type\": \"BY_ROLE\", \"roles\": [\"monitor\"] }\n" +
                "}";

        String res = mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long activityId = om.readTree(res).get("id").asLong();
        String attList = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // deve conter Maria (monitor) e não Paulo (preceptor)
        assertThat(attList).contains("maria@example.com");
        assertThat(attList).doesNotContain("paulo@example.com");
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void collective_by_list_targets_only_listed_users() throws Exception {
        // cria outro usuário também membro do GT
        User outro = new User();
        outro.setNome("Outro");
        outro.setEmail("outro@example.com");
        outro.setPasswordHash("x");
        userRepository.save(outro);
        UserGt ug = new UserGt();
        ug.setId(new UserGt.Id(outro.getId(), gtId));
        ug.setUser(outro);
        ug.setGt(gtRepository.findById(gtId).orElseThrow());
        ug.setRoleInGt("monitor");
        userGtRepository.save(ug);

        // target lista apenas o 'outro'
        String payload = "{\n" +
                "  \"tipo\": \"coletiva\",\n" +
                "  \"titulo\": \"Sessão\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"11:00\",\n" +
                "  \"fim\": \"12:00\",\n" +
                "  \"target\": { \"type\": \"BY_LIST\", \"users\": [" + outro.getId() + "] }\n" +
                "}";

        String res = mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long activityId = om.readTree(res).get("id").asLong();
        String attList = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(attList).contains("outro@example.com");
        assertThat(attList).doesNotContain("maria@example.com");
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void daily_limit_24h_enforced() throws Exception {
        // adiciona um aluno membro do GT
        User aluno = new User();
        aluno.setNome("Aluno");
        aluno.setEmail("aluno3@example.com");
        aluno.setPasswordHash("x");
        userRepository.save(aluno);
        UserGt ug = new UserGt();
        ug.setId(new UserGt.Id(aluno.getId(), gtId));
        ug.setUser(aluno);
        ug.setGt(gtRepository.findById(gtId).orElseThrow());
        ug.setRoleInGt("monitor");
        userGtRepository.save(ug);

        // cria coletiva 00:00-23:00 para todo GT (inclui aluno)
        String longAct = "{\n" +
                "  \"tipo\": \"coletiva\",\n" +
                "  \"titulo\": \"Longa\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"00:00\",\n" +
                "  \"fim\": \"23:00\",\n" +
                "  \"target\": { \"type\": \"ALL_GT\" }\n" +
                "}";
        mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .with(user("maria@example.com").roles("coordenador_geral"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(longAct))
                .andExpect(status().isOk());

        // como o aluno cria uma individual de 02:00 (excede 24h)
        String indiv = "{\n" +
                "  \"tipo\": \"individual\",\n" +
                "  \"titulo\": \"Extra\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"10:00\",\n" +
                "  \"fim\": \"12:00\"\n" +
                "}";
        // cria como aluno (monitor) e espera erro 400
        mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .with(user("aluno3@example.com").roles("monitor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(indiv))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void validate_attendance_changes_status_to_validada() throws Exception {
        // cria uma atividade individual por um aluno
        User aluno = new User();
        aluno.setNome("Aluno");
        aluno.setEmail("aluno2@example.com");
        aluno.setPasswordHash("x");
        userRepository.save(aluno);

        String create = "{\n" +
                "  \"tipo\": \"individual\",\n" +
                "  \"titulo\": \"Atividade\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"07:00\",\n" +
                "  \"fim\": \"08:00\"\n" +
                "}";

        String res = mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long activityId = om.readTree(res).get("id").asLong();
        String attListJson = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode listNode = om.readTree(attListJson);
        Long attendanceId = listNode.get(0).get("id").asLong();

        // valida como coordenador geral
        mvc.perform(post("/api/v1/attendances/" + attendanceId + "/validate"))
                .andExpect(status().isNoContent());

        String after = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(after).contains("validada");
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"coordenador_geral"})
    void reject_attendance_changes_status_to_rejeitada() throws Exception {
        // cria atividade individual de aluno
        User aluno = new User();
        aluno.setNome("Aluno");
        aluno.setEmail("aluno4@example.com");
        aluno.setPasswordHash("x");
        userRepository.save(aluno);

        String create = "{\n" +
                "  \"tipo\": \"individual\",\n" +
                "  \"titulo\": \"Atividade\",\n" +
                "  \"data\": \"2025-09-07\",\n" +
                "  \"inicio\": \"07:00\",\n" +
                "  \"fim\": \"08:00\"\n" +
                "}";

        String res = mvc.perform(post("/api/v1/gts/" + gtId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long activityId = om.readTree(res).get("id").asLong();
        String attListJson = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode listNode = om.readTree(attListJson);
        Long attendanceId = listNode.get(0).get("id").asLong();

        mvc.perform(post("/api/v1/attendances/" + attendanceId + "/reject"))
                .andExpect(status().isNoContent());

        String after = mvc.perform(get("/api/v1/activities/" + activityId + "/attendances"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(after).contains("rejeitada");
    }
}

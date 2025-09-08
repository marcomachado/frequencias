package com.petsaudedigital.backend.repository;

import com.petsaudedigital.backend.domain.*;
import com.petsaudedigital.backend.domain.enums.ActivityType;
import com.petsaudedigital.backend.domain.enums.AttendanceMode;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActivityAttendanceRepositoryTest {

    @Autowired ProjectRepository projectRepository;
    @Autowired AxisRepository axisRepository;
    @Autowired GtRepository gtRepository;
    @Autowired UserRepository userRepository;
    @Autowired ActivityRepository activityRepository;
    @Autowired AttendanceRepository attendanceRepository;

    @Test
    void create_activity_and_attendance_unique_per_user() {
        // user
        User u = new User();
        u.setNome("João");
        u.setEmail("joao@example.com");
        u.setPasswordHash("x");
        userRepository.save(u);

        // project -> axis -> gt
        Project p = new Project();
        p.setNome("Projeto X");
        p.setDescricao("Desc");
        p.setAtivo(1);
        p.setCreatedAt("2025-09-07T00:00:00");
        projectRepository.save(p);

        Axis a = new Axis();
        a.setProject(p);
        a.setNome("Formação");
        a.setAtivo(1);
        axisRepository.save(a);

        Gt gt = new Gt();
        gt.setProject(p);
        gt.setAxis(a);
        gt.setNome("GT A");
        gt.setAtivo(1);
        gtRepository.save(gt);

        Activity act = new Activity();
        act.setProject(p);
        act.setGt(gt);
        act.setTipo(ActivityType.coletiva);
        act.setTitulo("Reunião");
        act.setData("2025-09-07");
        act.setInicio("10:00");
        act.setFim("12:00");
        act.setCreatedBy(u);
        activityRepository.save(act);

        Attendance att = new Attendance();
        att.setActivity(act);
        att.setUser(u);
        att.setStatus(AttendanceStatus.validada);
        att.setModo(AttendanceMode.auto);
        att.setCreatedAt("2025-09-07T12:00:00");
        attendanceRepository.save(att);

        List<Attendance> list = attendanceRepository.findByActivity(act);
        assertThat(list).hasSize(1);

        Attendance dup = new Attendance();
        dup.setActivity(act);
        dup.setUser(u);
        dup.setStatus(AttendanceStatus.validada);
        dup.setModo(AttendanceMode.auto);
        dup.setCreatedAt("2025-09-07T12:05:00");

        assertThrows(DataIntegrityViolationException.class, () -> {
            attendanceRepository.saveAndFlush(dup);
        });
    }
}


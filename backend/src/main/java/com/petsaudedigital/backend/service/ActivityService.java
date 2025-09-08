package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.ActivityDtos;
import com.petsaudedigital.backend.domain.*;
import com.petsaudedigital.backend.domain.enums.ActivityType;
import com.petsaudedigital.backend.domain.enums.AttendanceMode;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import com.petsaudedigital.backend.domain.enums.TargetType;
import com.petsaudedigital.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ActivityTargetRepository activityTargetRepository;
    private final ActivityTargetRoleRepository activityTargetRoleRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final GtRepository gtRepository;
    private final UserGtRepository userGtRepository;

    public Activity create(Long gtId, ActivityDtos.Create req, Authentication authentication) {
        Gt gt = gtRepository.findById(gtId).orElseThrow();
        Project project = gt.getProject();
        User creator = userRepository.findByEmail(authentication.getName()).orElseThrow();

        if (!LocalTime.parse(req.fim()).isAfter(LocalTime.parse(req.inicio()))) {
            throw new IllegalArgumentException("fim deve ser > inicio");
        }

        Activity a = new Activity();
        a.setProject(project);
        a.setGt(gt);
        a.setSubgroup(null);
        a.setTipo(req.tipo());
        a.setTitulo(req.titulo());
        a.setData(req.data());
        a.setInicio(req.inicio());
        a.setFim(req.fim());
        a.setLocal(req.local());
        a.setDescricao(req.descricao());
        a.setEvidenciasJson(req.evidencias() == null ? null : req.evidencias().toString());
        a.setCreatedBy(creator);
        activityRepository.save(a);

        if (req.tipo() == ActivityType.coletiva) {
            boolean isPrivileged = authentication.getAuthorities().stream()
                    .map(ga -> ga.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_coordenador_geral") || auth.equals("ROLE_coordenador_eixo") || auth.equals("ROLE_coordenador_gt") || auth.equals("ROLE_tutor"));
            if (!isPrivileged) {
                throw new org.springframework.security.access.AccessDeniedException("Somente Coordenador/Tutor podem lançar coletivas");
            }
            applyTargetAndAutoAttendances(a, req.target());
        } else {
            // individual: presença do próprio autor pendente, exceto coord/tutor
            Attendance att = new Attendance();
            att.setActivity(a);
            att.setUser(creator);
            boolean isPrivileged = authentication.getAuthorities().stream()
                    .anyMatch(ga -> {
                        String auth = ga.getAuthority();
                        return auth.equals("ROLE_coordenador_geral") || auth.equals("ROLE_coordenador_eixo") || auth.equals("ROLE_coordenador_gt") || auth.equals("ROLE_tutor");
                    });
            att.setStatus(isPrivileged ? AttendanceStatus.validada : AttendanceStatus.pendente);
            att.setModo(AttendanceMode.manual);
            att.setCreatedAt(java.time.Instant.now().toString());
            enforceDailyLimit(a, creator, att);
            attendanceRepository.save(att);
        }

        return a;
    }

    public java.util.Optional<Activity> patch(Long id, Activity patch) {
        return activityRepository.findById(id).map(existing -> {
            if (patch.getTitulo() != null) existing.setTitulo(patch.getTitulo());
            if (patch.getLocal() != null) existing.setLocal(patch.getLocal());
            if (patch.getDescricao() != null) existing.setDescricao(patch.getDescricao());
            if (patch.getEvidenciasJson() != null) existing.setEvidenciasJson(patch.getEvidenciasJson());
            return activityRepository.save(existing);
        });
    }

    private void applyTargetAndAutoAttendances(Activity a, ActivityDtos.Target target) {
        if (target == null) throw new IllegalArgumentException("Target é obrigatório para atividade coletiva");
        ActivityTarget t = new ActivityTarget();
        t.setActivityId(a.getId());
        t.setTargetType(target.type());
        activityTargetRepository.save(t);

        List<User> participants;
        if (target.type() == TargetType.ALL_GT) {
            participants = userGtRepository.findAll().stream()
                    .filter(ug -> ug.getGt().getId().equals(a.getGt().getId()))
                    .map(UserGt::getUser)
                    .distinct()
                    .collect(Collectors.toList());
        } else if (target.type() == TargetType.BY_ROLE) {
            Set<String> roles = new HashSet<>(Optional.ofNullable(target.roles()).orElse(List.of()));
            for (String r : roles) {
                ActivityTargetRole atr = new ActivityTargetRole();
                atr.setId(new ActivityTargetRole.Id(a.getId(), r));
                atr.setActivity(a);
                activityTargetRoleRepository.save(atr);
            }
            participants = userGtRepository.findAll().stream()
                    .filter(ug -> ug.getGt().getId().equals(a.getGt().getId()) && roles.contains(ug.getRoleInGt()))
                    .map(UserGt::getUser)
                    .distinct().collect(Collectors.toList());
        } else if (target.type() == TargetType.BY_LIST) {
            List<Long> ids = Optional.ofNullable(target.users()).orElse(List.of());
            participants = userRepository.findAllById(ids);
            for (Long uid : ids) {
                ActivityParticipant ap = new ActivityParticipant();
                ap.setId(new ActivityParticipant.Id(a.getId(), uid));
                ap.setActivity(a);
                ap.setUser(participants.stream().filter(u -> u.getId().equals(uid)).findFirst().orElse(null));
                activityParticipantRepository.save(ap);
            }
        } else {
            throw new IllegalArgumentException("Target inválido");
        }

        for (User u : participants) {
            Attendance att = new Attendance();
            att.setActivity(a);
            att.setUser(u);
            att.setStatus(AttendanceStatus.validada);
            att.setModo(AttendanceMode.auto);
            att.setCreatedAt(java.time.Instant.now().toString());
            enforceDailyLimit(a, u, att);
            attendanceRepository.save(att);
        }
    }

    private void enforceDailyLimit(Activity a, User u, Attendance newAttendance) {
        // Somatório diário ≤ 24h: verificamos todas as atividades do usuário na data
        String date = a.getData();
        LocalTime start = LocalTime.parse(a.getInicio());
        LocalTime end = LocalTime.parse(a.getFim());
        long minutes = Duration.between(start, end).toMinutes();

        // Buscar todas as presenças do usuário neste dia
        List<Attendance> existing = attendanceRepository.findAll().stream()
                .filter(att -> att.getUser().getId().equals(u.getId()))
                .filter(att -> att.getActivity().getData().equals(date))
                .toList();

        long totalMinutes = existing.stream()
                .map(Attendance::getActivity)
                .mapToLong(ac -> Duration.between(LocalTime.parse(ac.getInicio()), LocalTime.parse(ac.getFim())).toMinutes())
                .sum();

        if (totalMinutes + minutes > 24 * 60) {
            throw new IllegalArgumentException("Limite diário de 24h excedido para o usuário");
        }
    }
}

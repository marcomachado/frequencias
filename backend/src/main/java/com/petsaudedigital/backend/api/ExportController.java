package com.petsaudedigital.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.petsaudedigital.backend.api.dto.ExportDtos;
import com.petsaudedigital.backend.domain.*;
import com.petsaudedigital.backend.repository.*;
import com.petsaudedigital.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class ExportController {
    private final ObjectMapper om;
    private final ProjectRepository projectRepository;
    private final AxisRepository axisRepository;
    private final GtRepository gtRepository;
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserGtRepository userGtRepository;
    private final UserRepository userRepository;
    private final ExportLogRepository exportLogRepository;
    private final AuditService auditService;

    @PostMapping("/api/v1/exports/json")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', #req.scopeType, #req.scopeId)")
    public ResponseEntity<ObjectNode> export(@RequestBody ExportDtos.Request req, Authentication auth) {
        // Resolve GTs a exportar
        Set<Long> gtIds = new HashSet<>(Optional.ofNullable(req.gtIds()).orElse(List.of()));
        if (req.scopeType() != null && "GT".equals(req.scopeType()) && req.scopeId() != null) {
            gtIds.add(req.scopeId());
        }
        List<Gt> gts = gtIds.isEmpty() ? gtRepository.findAll() : gtRepository.findAllById(gtIds);

        // Montar estrutura meta
        ObjectNode root = om.createObjectNode();
        ObjectNode meta = root.putObject("meta");
        meta.put("schema_version", Optional.ofNullable(req.schemaVersion()).orElse("1.2.0"));
        meta.put("generated_at", Instant.now().toString());
        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(u -> {
                ObjectNode by = meta.putObject("generated_by");
                by.put("id", u.getId()); by.put("nome", u.getNome()); by.put("email", u.getEmail());
            });
        }
        if (req.scopeType() != null) {
            ObjectNode scope = meta.putObject("scope");
            scope.put("type", req.scopeType());
            if (req.scopeId() != null) scope.put("id", req.scopeId());
        }
        ObjectNode filters = meta.putObject("filters");
        if (req.from() != null) filters.put("from", req.from());
        if (req.to() != null) filters.put("to", req.to());
        if (!gtIds.isEmpty()) {
            ArrayNode arr = filters.putArray("gt_ids");
            gtIds.forEach(arr::add);
        }

        // Organizar por eixo
        ArrayNode eixos = root.putArray("eixos");
        Map<Long, ObjectNode> axisJsonById = new HashMap<>();
        for (Gt gt : gts) {
            Axis axis = gt.getAxis();
            ObjectNode axisNode = axisJsonById.computeIfAbsent(axis.getId(), id -> {
                ObjectNode an = om.createObjectNode();
                an.put("id", axis.getId());
                an.put("nome", axis.getNome());
                an.set("gts", om.createArrayNode());
                eixos.add(an);
                return an;
            });
            ArrayNode axisGts = (ArrayNode) axisNode.get("gts");
            ObjectNode gtNode = om.createObjectNode();
            gtNode.put("id", gt.getId());
            gtNode.put("nome", gt.getNome());
            // subgrupos (simplificado)
            gtNode.set("subgrupos", om.createArrayNode());

            // usuarios do GT
            ArrayNode usuarios = gtNode.putArray("usuarios");
            userGtRepository.findAll().stream()
                    .filter(ug -> ug.getGt().getId().equals(gt.getId()))
                    .map(UserGt::getUser)
                    .distinct()
                    .forEach(u -> {
                        ObjectNode un = om.createObjectNode();
                        un.put("id", u.getId());
                        un.put("nome", u.getNome());
                        un.put("email", u.getEmail());
                        usuarios.add(un);
                    });

            // atividades no período
            ArrayNode atividades = gtNode.putArray("atividades");
            for (Activity a : activityRepository.findByGt(gt)) {
                if (req.from() != null && a.getData().compareTo(req.from()) < 0) continue;
                if (req.to() != null && a.getData().compareTo(req.to()) > 0) continue;
                ObjectNode an = om.createObjectNode();
                an.put("id", a.getId());
                an.put("tipo", a.getTipo().name());
                an.put("titulo", a.getTitulo());
                an.put("data", a.getData());
                an.put("inicio", a.getInicio());
                an.put("fim", a.getFim());
                ArrayNode presencas = an.putArray("presencas");
                for (Attendance at : attendanceRepository.findByActivity(a)) {
                    ObjectNode pn = om.createObjectNode();
                    pn.put("user_id", at.getUser().getId());
                    pn.put("status", at.getStatus().name());
                    pn.put("modo", at.getModo().name());
                    presencas.add(pn);
                }
                atividades.add(an);
            }

            axisGts.add(gtNode);
        }

        // Log de export
        ExportLog log = new ExportLog();
        if (auth != null) userRepository.findByEmail(auth.getName()).ifPresent(log::setActor);
        log.setScopeType(req.scopeType());
        log.setScopeId(req.scopeId());
        log.setFiltersJson(om.createObjectNode()
                .put("from", Optional.ofNullable(req.from()).orElse(""))
                .put("to", Optional.ofNullable(req.to()).orElse(""))
                .putPOJO("gt_ids", gtIds)
                .toString());
        log.setSchemaVersion(meta.get("schema_version").asText());
        log.setCreatedAt(Instant.now().toString());
        exportLogRepository.save(log);

        auditService.log(auth, "export_json", "export", log.getId(), null);

        return ResponseEntity.ok(root);
    }
}


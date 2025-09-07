# PET Saúde Digital — SPEC (v1)

**Stack**: Java 21 + Spring Boot 3 (Web, Security, Validation, Data JPA, Flyway, MapStruct, Lombok, JWT), **SQLite** (xerial, Hibernate dialect), **Angular 18** (SPA, Angular Material).  
**Arquitetura**: Backend REST + Frontend SPA.  
**Data**: 2025-09-07

---

## 1. Visão Geral

Sistema para gestão do Projeto **PET Saúde Digital**, com hierarquia **Projeto → Eixos → GTs → Subgrupos**, controle de usuários e papéis, registro de **atividades** (coletivas/individuais), **presenças** e **validações**, geração de **folha de frequência (PDF)**, **relatórios**, **exportação JSON** e **auditoria**.

**Objetivos**:
- Segurança (RBAC por escopo), rastreabilidade (logs) e usabilidade.
- Automação de presenças e cálculos de horas com regras de negócio claras.
- Exportações consistentes com metadados e versionamento de schema.

---

## 2. Papéis, Escopos e Permissões

**Papéis**:
- **Coordenador Geral** (GLOBAL): cria **Projeto**, **Eixos**; pode coordenar GT; acesso total.
- **Coordenador de Eixo** (EIXO): gerencia GTs do seu eixo; pode coordenar GT do eixo.
- **Coordenador de GT** (GT): coordena um GT específico (inclui gestão de subgrupos).
- **Tutor** (GT): **pertence a um único GT**; lança coletivas, valida presenças/atividades.
- **Orientador de Serviço**, **Preceptor**, **Monitor** (GT).

**Permissões**:
- **Somente Coordenadores** (Geral/Eixo/GT) **concedem/removem** permissões extras (escopo + validade opcional).
- Padrão: Coordenadores e Tutores já possuem:
  - `validar_frequencia`
  - `lancar_presenca_validada`
  - `lancar_atividade_coletiva`
  - `exportar_dados`

**Escopos**: `GLOBAL | AXIS | GT`.

---

## 3. Requisitos Funcionais (RF)

### RF-01 Autenticação & Segurança
- Login (email+senha), JWT (access+refresh), expiração de sessão, bloqueio por tentativas.
- Autorização por **RBAC + escopo** via anotações (`@PreAuthorize`) e `PermissionEvaluator/ScopeService`.

### RF-02 Cadastro Hierárquico
- **Projeto**: criado pelo Coordenador Geral.
- **Eixo**: criado pelo Coordenador Geral; define Coordenador de Eixo.
- **GT**: criado no Eixo; define Coordenador do GT; **Tutor** vincula-se a **um único GT**.
- **Subgrupos**: livres, pertencem ao GT, **sem gestão própria** (coordenados pelo Coord. do GT).
- **Usuários** no GT: cadastrados por Coordenador/Tutor, com papel correto.

### RF-03 Atividades
- **Campos mínimos**: título, data, início, fim, local, descrição opcional, evidências opcionais.
- **Coletivas**:
  - Lançadas por **Coordenador/Tutor**.
  - Endereçadas a **todo GT** (`ALL_GT`), **por função** (`BY_ROLE`, ex.: `monitor`) ou **lista explícita** (`BY_LIST`).
  - Geram **presenças automáticas já válidas** para participantes indicados.
- **Individuais**:
  - Lançadas pelo próprio usuário; ficam **pendentes**.
  - **Validadas** por Coordenador/Tutor (ou Coord. de Eixo/Geral) **do mesmo escopo**.
  - **Exceção**: se o autor for **Coordenador/Tutor**, **dispensa validação** (fica válida).
- **Limites**: somatório diário por usuário **≤ 24h**.

### RF-04 Presenças e Validação
- Edição pelo autor **antes** da validação; após validação, apenas Coord/Tutor (ou quem tenha permissão) corrige.
- Coletivas de Coord/Tutor entram **validadas**.

### RF-05 Folha de Frequência
- Emissão por período; soma horas; sinalizar **“horas insuficientes”** quando < mínimo.
- Mínimo mensal **padrão 32h** (configurável pelo **Coord. Geral** por período/escopo).
- PDF com linhas para **Assinatura do Bolsista** e **Coordenador/Tutor**.

### RF-06 Relatórios
- Por GT, Eixo e Projeto: horas por usuário; usuários < mínimo; pendências; comparativos.

### RF-07 Exportação JSON
- Escopo por papel (GT/Eixo/Global); filtros (período, eixo, gt, subgrupo, usuário, status, tipo).
- Metadados: data/hora, autor, escopo, filtros, `schema_version`.
- Conteúdo: usuários, eixos/GTs/subgrupos, atividades (com participantes), presenças (status/validação).

### RF-08 Auditoria (Logs)
- Registrar: CRUD de usuário/atividade/presença, validações, exportações, permissões, configurações.
- Campos: ator, ação, entidade, id, `payload_diff`, `created_at`.

---

## 4. Requisitos Não Funcionais (RNF)

- **RNF-01**: Backend Spring Boot 3; Frontend Angular 18 (SPA).
- **RNF-02**: Banco **SQLite** (xerial), migrations via **Flyway**.
- **RNF-03**: Segurança (TLS, hash de senha, JWT curto + refresh, CORS restrito).
- **RNF-04**: Performance: operações comuns < **3s**; paginação padrão.
- **RNF-05**: Observabilidade: logs estruturados; correlação; métricas básicas.
- **RNF-06**: Manutenibilidade: versionamento API (`/api/v1`), testes, migrações versionadas.
- **RNF-07**: Internacionalização pronta (mensagens isoladas).

---

## 5. Modelo de Dados (MER — textual)

**Principais entidades**:
- `project(id, nome, descricao, ativo, created_at)`
- `axis(id, project_id FK, nome, coord_eixo_user_id FK, ativo)`
- `gt(id, project_id FK, axis_id FK, nome, coord_gt_user_id FK, ativo)`
- `subgroup(id, project_id FK, gt_id FK, nome, descricao)`
- `user(id, nome, email, password_hash, contato_principal, contatos_json, formacao, vinculo, ativo)`

**Papéis e permissões**:
- `user_roles(user_id, role)` — `coordenador_geral | coordenador_eixo | coordenador_gt | tutor | orientador_servico | preceptor | monitor`
- `role_scopes(user_id, role, scope_type: GLOBAL|AXIS|GT, scope_id)`
- `user_permissions(user_id, permission, scope_type, scope_id, valid_from, valid_until, granted_by, granted_at)`

**Participação & subgrupo**:
- `user_gt(user_id, gt_id, role_in_gt)` — garante “Tutor em 1 GT”.
- `user_subgroup(user_id, subgroup_id)`

**Atividades & presenças**:
- `activity(id, project_id, gt_id, subgroup_id?, tipo: coletiva|individual, titulo, data, inicio, fim, local, descricao, evidencias_json, created_by)`
- `activity_target(activity_id, target_type: ALL_GT|BY_ROLE|BY_LIST)`
- `activity_target_role(activity_id, role)`
- `activity_participant(activity_id, user_id)`
- `attendance(id, activity_id, user_id, status: pendente|validada|rejeitada, modo: auto|manual, created_at)`
- `attendance_validation(attendance_id, validator_user_id, decision: validar|rejeitar, validated_at, note)`

**Config/Auditoria/Exportação**:
- `system_settings(project_id, key, value, effective_from, effective_to, scope_type, scope_id)`
- `audit_log(id, actor_id, action, entity, entity_id, payload_diff, created_at)`
- `export_log(id, actor_id, scope_type, scope_id, filters_json, schema_version, created_at)`

**Cardinalidades**:
- project 1—N axis; axis 1—N gt; gt 1—N subgroup; gt N—N user (via `user_gt`).
- activity 1—N attendance; activity N—N user (via `activity_participant`).

---

## 6. API v1 (endpoints)

### 6.1 Auth
- `POST /api/v1/auth/login` → { email, senha } → { access_token, refresh_token, expires_in }
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### 6.2 Projetos / Eixos / GTs / Subgrupos
- `POST /api/v1/projects` (Coord Geral); `GET /api/v1/projects`
- `POST /api/v1/projects/{projectId}/axes` (Coord Geral); `GET /api/v1/projects/{projectId}/axes`
- `POST /api/v1/axes/{axisId}/gts` (Coord Geral/Coord Eixo); `GET /api/v1/axes/{axisId}/gts`
- `POST /api/v1/gts/{gtId}/subgroups` (Coord GT); `GET /api/v1/gts/{gtId}/subgroups`

### 6.3 Membros, Papéis e Permissões
- `POST /api/v1/gts/{gtId}/members` (Coord GT/Tutor) — adiciona usuário com papel no GT
- `PATCH /api/v1/gts/{gtId}/members/{userId}` — altera papel no GT
- `POST /api/v1/permissions/grant` (apenas Coordenadores)
- `POST /api/v1/permissions/revoke` (apenas Coordenadores)

### 6.4 Atividades
- `POST /api/v1/gts/{gtId}/activities`  
  - **coletiva**: `{ tipo:'coletiva', titulo, data, inicio, fim, local, target: {type:'ALL_GT'|'BY_ROLE'|'BY_LIST', roles?:[], users?:[]}, evidencias:[] }`  
  - **individual**: `{ tipo:'individual', titulo, data, inicio, fim, local, evidencias:[] }`
- `GET /api/v1/gts/{gtId}/activities?from&to&tipo`
- `GET /api/v1/activities/{activityId}` / `PATCH` / `DELETE`

### 6.5 Presenças
- `GET /api/v1/activities/{activityId}/attendances`
- `POST /api/v1/activities/{activityId}/attendances` (quando lançamento manual permitido)
- `POST /api/v1/attendances/{attendanceId}/validate` (Coord/Tutor)
- `POST /api/v1/attendances/{attendanceId}/reject` (Coord/Tutor)

### 6.6 Timesheet e Relatórios
- `GET /api/v1/users/{userId}/timesheet?from&to` → resumo+itens+flag `horas_insuficientes`
- `POST /api/v1/users/{userId}/timesheet/pdf`
- `GET /api/v1/reports/hours-by-user?gtId&month`
- `GET /api/v1/reports/pending-attendances?gtId`

### 6.7 Exportação
- `POST /api/v1/exports/json` — filtros + meta + `schema_version`, registra em `export_log`

---

## 7. Regras de Validação

- `fim > inicio`; duração resultante **> 0**.
- **Somatório diário ≤ 24h** por usuário (considerando todas as atividades do dia).
- Atividade **individual** criada por não-Coord/Tutor → presença `pendente`.
- Validador deve ter **escopo compatível** (mesmo GT ou superior — Axis/Global).
- Tutor **não pode** estar vinculado a mais de **1 GT**.

---

## 8. Estrutura de Exportação JSON (esqueleto)

```json
{
  "meta": {
    "schema_version": "1.2.0",
    "generated_at": "2025-09-07T12:00:00Z",
    "generated_by": {"id": 1, "nome": "Admin", "papel": "coordenador_geral"},
    "scope": {"type": "AXIS", "id": 3},
    "filters": {"from": "2025-09-01", "to": "2025-09-30", "gt_ids": [7,8]}
  },
  "eixos": [
    {
      "id": 3,
      "nome": "Formação",
      "gts": [
        {
          "id": 7,
          "nome": "GT Formação A",
          "subgrupos": [{"id": 22, "nome": "Turma Alfa"}],
          "usuarios": [{"id": 101, "nome": "Maria", "papeis": ["tutor"], "vinculo": "UF-XYZ"}],
          "atividades": [
            {
              "id": 501,
              "tipo": "coletiva",
              "titulo": "Reunião semanal",
              "data": "2025-09-05",
              "inicio": "14:00",
              "fim": "16:00",
              "duracao_h": 2,
              "local": "Sala 101",
              "participantes": [101,102,103],
              "evidencias": [],
              "presencas": [
                {"user_id": 101, "status": "validada", "modo": "auto", "validado_por": 456, "validado_em": "2025-09-05T16:05:00"}
              ]
            }
          ]
        }
      ]
    }
  ]
}

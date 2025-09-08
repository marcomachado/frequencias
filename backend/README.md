# PET Saúde Digital — Backend

Módulo Spring Boot (Java 21) do PET Saúde Digital.
Status: Auth (JWT access/refresh) + RBAC por escopo; domínio JPA; API v1 parcial (hierarquia, membros/permissões, atividades/presenças).

## Requisitos
- Java 21 (Temurin recomendado)
- Maven 3.9+

## Rodando localmente

1) Configurar o banco SQLite (opcional):

```bash
export SQLITE_DB_PATH=./data/petsaude.db
```

2) Executar a aplicação:

```bash
mvn spring-boot:run
```

3) Build completo com testes:

```bash
mvn -q -DskipTests=false verify
```

## Banco de Dados e Migrations
- Banco: SQLite (xerial). URL default: `jdbc:sqlite:./data/petsaude.db`.
- Migrações: Flyway (V1 em `src/main/resources/db/migration/V1__init.sql`, V2 refresh tokens).
- `ddl-auto=validate` (nenhuma criação automática por JPA).

## Configurações (application.yml)
- `spring.datasource.url`: JDBC do SQLite (padrão aponta para `./data/petsaude.db`).
- `spring.jpa.hibernate.ddl-auto`: `validate`.
- `spring.flyway.enabled`: `true`.
- `security.jwt.secret` (recomendado via `JWT_SECRET`), `access-token-ttl-seconds`, `refresh-token-ttl-seconds`.

## Endpoints v1 (resumo)
- Auth: `POST /api/v1/auth/login`, `/auth/refresh`, `/auth/logout`
- Projects: `POST /api/v1/projects`, `GET /api/v1/projects`
- Axes: `POST /api/v1/projects/{projectId}/axes`, `GET /api/v1/projects/{projectId}/axes`
- GTs: `POST /api/v1/axes/{axisId}/gts`, `GET /api/v1/axes/{axisId}/gts`
- Subgroups: `POST /api/v1/gts/{gtId}/subgroups`, `GET /api/v1/gts/{gtId}/subgroups`
- Members: `POST /api/v1/gts/{gtId}/members`, `PATCH /api/v1/gts/{gtId}/members/{userId}`
- Permissions: `POST /api/v1/permissions/grant`, `POST /api/v1/permissions/revoke`
- Activities: `POST /api/v1/gts/{gtId}/activities`, `GET /api/v1/activities/{id}`, `PATCH/DELETE /api/v1/activities/{id}`
- Attendances: `GET /api/v1/activities/{id}/attendances`, `POST /api/v1/attendances/{id}/validate`, `POST /api/v1/attendances/{id}/reject`

Observação de segurança: nos endpoints sensíveis usamos `@PreAuthorize("@scope.has(authentication, 'exportar_dados', #scopeType, #scopeId)")` ou resolvemos o escopo via `@scopeResolver`.

## Exemplos curl
Autenticação (JWT):

```bash
# Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","senha":"secret"}'

# Refresh
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refresh_token":"<REFRESH_TOKEN>"}'

# Logout
curl -i -X POST http://localhost:8080/api/v1/auth/logout \
  -H 'Content-Type: application/json' \
  -d '{"refresh_token":"<REFRESH_TOKEN>"}'
```

Hierarquia (requer token Bearer com permissões adequadas):

```bash
# Criar projeto
curl -s -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"nome":"Projeto PET","descricao":"Piloto"}'

# Criar eixo
curl -s -X POST http://localhost:8080/api/v1/projects/1/axes \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"nome":"Formação"}'

# Criar GT
curl -s -X POST http://localhost:8080/api/v1/axes/1/gts \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"nome":"GT A"}'

# Criar Subgrupo
curl -s -X POST http://localhost:8080/api/v1/gts/1/subgroups \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"nome":"Turma Alfa"}'

# Adicionar membro ao GT
curl -s -X POST http://localhost:8080/api/v1/gts/1/members \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"userId": 10, "roleInGt": "monitor"}'
```

Atividades e presenças:

```bash
# Coletiva para todo GT (auto-validadas)
curl -s -X POST http://localhost:8080/api/v1/gts/1/activities \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"tipo":"coletiva","titulo":"Reunião","data":"2025-09-07","inicio":"14:00","fim":"16:00","target":{"type":"ALL_GT"}}'

# Listar presenças
curl -s http://localhost:8080/api/v1/activities/1/attendances \
  -H "Authorization: Bearer $ACCESS"

# Validar presença
curl -i -X POST http://localhost:8080/api/v1/attendances/1/validate \
  -H "Authorization: Bearer $ACCESS"
```

## Plano (Roadmap resumido)
1. Scaffold + Flyway V1 (concluído)
2. Auth (JWT) + RBAC escopo (concluído)
3. Domínio (Entities/Repos/Services) (concluído)
4. API v1 (Projects/Eixos/GTs/Subgrupos, Members/Permissions) (concluído)
5. Activities/Attendance + regras (parcial: CRUD básico + validação)
6. Timesheet (PDF), Export JSON, Audit logs (próximo)
7. Seeds e testes e2e (próximo)

## Referências
- docs/SPEC.md

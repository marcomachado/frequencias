# feat: security + domain + api v1 (hierarquia, activities/attendance)

## Plano do PR
- Autenticação JWT (access/refresh) + RBAC por escopo
- Domínio JPA (MER completo) + Repositórios + MapStruct
- API v1: Projects/Eixos/GTs/Subgrupos, Members, Permissions
- Activities/Attendance: coletivas auto-validadas, individuais pendentes (exc. coord/tutor), validação/rejeição
- Regras de negócio: limite 24h/dia por usuário
- Testes: login/refresh (Auth) + WebMvc básicos para Activities

## O que foi feito
- Security
  - SecurityConfig, filtro JWT (HS256 via Nimbus), JwtService
  - Endpoints: `POST /api/v1/auth/login`, `/auth/refresh`, `/auth/logout`
  - Refresh tokens persistidos (rotação/expiração) — Flyway V2
  - RBAC por escopo: `ScopeService.has(...)` + `PermissionEvaluator`
- Domínio/Infra
  - Entidades JPA conforme SPEC (users, project/axis/gt/subgroup, roles/scopes/permissions, activities/attendance)
  - Repositórios Spring Data
  - DTOs + MapStruct (Project/Axis/Gt/Subgroup)
- API v1
  - Projects/Eixos/GTs/Subgrupos (POST/GET)
  - Members (POST/PATCH) e Permissions (grant/revoke)
  - Activities: criação coletiva/individual; GET activity; GET attendances
  - Attendance: validate/reject
  - RBAC com `@PreAuthorize("@scope.has(authentication, 'exportar_dados', #scopeType, #scopeId)")` e resolvers de escopo
- Testes
  - Auth: login/refresh
  - Activities (WebMvc): ALL_GT, BY_ROLE, BY_LIST, individual pendente, validate/reject, 24h/dia

## Como testar localmente
- Requisitos: JDK 21, Maven 3.9+
- Build/tests: `./mvnw -DskipTests=false verify`
- Run: `./mvnw spring-boot:run`
- Configurar segredos (dev): `JWT_SECRET`, opcional `SQLITE_DB_PATH`

## Notas
- Ambiente deste PR não executou build por ausência de JDK 21; no CI/local com Temurin 21 deve passar.
- Próximos PRs (sugestão): Timesheet+PDF, Export JSON+audit, Seeds+Cobertura.


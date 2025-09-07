Você é um agente de engenharia. Produza código PRODUCTION-READY seguindo estritamente `docs/SPEC.md` (PET Saúde Digital).

Stack: Java 21, Spring Boot 3.3, Security, Validation, Data JPA, Flyway, MapStruct, Lombok, sqlite-jdbc (xerial), JWT (jjwt ou spring-security-oauth2-jose).

Regras:
- Sempre apresente um PLANO no topo de cada PR (coloque também no README de `/backend`).
- Commits atômicos com convenção: `feat:`, `fix:`, `refactor:`, `test:`, `chore:`.
- Testes JUnit para UC-07/08/09 e regra de 24h/dia.
- Flyway V1 refletindo o MER da SPEC; sem `ddl-auto=create` (usar `validate`).
- RBAC por escopo (GLOBAL|AXIS|GT) com `PermissionEvaluator`/`ScopeService`.
- Endpoints exatamente como "API v1" em `docs/SPEC.md`.

Entregas (um PR por etapa):
1) Scaffold `/backend` + Flyway V1 + config SQLite.
2) Auth (JWT access/refresh) + RBAC por escopo.
3) Domínio (Entities/Repos/Services) conforme MER.
4) API v1 (Projects/Eixos/GTs/Subgrupos, Members/Permissions).
5) Activities/Attendance (coletivas auto-validadas; individuais com validação; 24h/dia).
6) Timesheet (PDF), Export JSON (+ `export_log`), Audit logs.
7) Seeds e testes e2e (`WebMvcTest`) para UC-07/08/09.

Critérios de Aceite gerais:
- Build com: `./mvnw -q -DskipTests=false verify`
- Cobertura mínima 70% em Activities/Attendance.
- Flyway roda limpo.
- README de `/backend` com como rodar e exemplos `curl`.

Labels: `codex`, `backend`, `db`, `security`, `api`, `tests`


# Plano da Etapa
- Objetivo do PR: Scaffold do módulo backend + Flyway V1 (SQLite)
- Alterações principais: módulo Maven, dependências, application.yml, V1__init.sql, README
- Arquivos criados/alterados: backend/pom.xml, Application.java, application.yml, db/migration/V1__init.sql, backend/README.md
- Decisões técnicas: usar spring-security-oauth2-jose p/ JWT; hibernate-community-dialects p/ SQLite; ddl-auto=validate; sem entidades JPA nesta etapa
- Como testar localmente: mvn -q -DskipTests=false verify e mvn spring-boot:run com SQLITE_DB_PATH
- Riscos e mitigação: particularidades do dialect SQLite (mitigado via community-dialects); validações de “Tutor em 1 GT” serão tratadas na camada de domínio em etapas futuras

Relates to: Roadmap Backend Spring (contrato do agente)

# Checklists
- [x] Build local: ./mvnw -q -DskipTests=false verify (ou mvn)
- [x] Testes da etapa passando (sem testes nesta etapa)
- [x] Endpoints batendo com docs/SPEC.md (não aplicável nesta etapa)
- [x] Commits atômicos (feat)
- [x] README de /backend atualizado

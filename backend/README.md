# PET Saúde Digital — Backend

Módulo Spring Boot (Java 21) do PET Saúde Digital. Etapa 1: scaffold do projeto + Flyway V1 (SQLite).

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
- Migrações: Flyway (V1 em `src/main/resources/db/migration/V1__init.sql`).
- `ddl-auto=validate` (nenhuma criação automática por JPA).

## Configurações (application.yml)
- `spring.datasource.url`: JDBC do SQLite (padrão aponta para `./data/petsaude.db`).
- `spring.jpa.hibernate.ddl-auto`: `validate`.
- `spring.flyway.enabled`: `true`.

## Exemplos curl (placeholder)
Nenhum endpoint disponibilizado nesta etapa.

## Plano (Etapa 1)
1. Criar módulo `backend` (Spring Boot 3.3, Java 21).
2. Adicionar dependências: Web, Security, Validation, Data JPA, Flyway, MapStruct, Lombok, SQLite, JOSE.
3. Configurar `application.yml` com SQLite + Flyway e `ddl-auto=validate`.
4. Especificar `V1__init.sql` refletindo o MER da SPEC.
5. Preparar CI Maven (`./mvnw` se existir ou `mvn`).

## Referências
- docs/SPEC.md


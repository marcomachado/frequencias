-- Flyway V1 - Initial schema for PET Saúde Digital
-- Based on docs/SPEC.md (MER textual)
PRAGMA foreign_keys = ON;

-- USERS
CREATE TABLE IF NOT EXISTS "user" (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    nome             TEXT NOT NULL,
    email            TEXT NOT NULL UNIQUE,
    password_hash    TEXT NOT NULL,
    contato_principal TEXT,
    contatos_json    TEXT,
    formacao         TEXT,
    vinculo          TEXT,
    ativo            INTEGER NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_user_email ON "user" (email);

-- PROJECT
CREATE TABLE IF NOT EXISTS project (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nome        TEXT NOT NULL,
    descricao   TEXT,
    ativo       INTEGER NOT NULL DEFAULT 1,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

-- AXIS
CREATE TABLE IF NOT EXISTS axis (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id           INTEGER NOT NULL,
    nome                 TEXT NOT NULL,
    coord_eixo_user_id   INTEGER,
    ativo                INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE RESTRICT,
    FOREIGN KEY (coord_eixo_user_id) REFERENCES "user"(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_axis_project ON axis(project_id);
CREATE INDEX IF NOT EXISTS idx_axis_coord_user ON axis(coord_eixo_user_id);

-- GT
CREATE TABLE IF NOT EXISTS gt (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id         INTEGER NOT NULL,
    axis_id            INTEGER NOT NULL,
    nome               TEXT NOT NULL,
    coord_gt_user_id   INTEGER,
    ativo              INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE RESTRICT,
    FOREIGN KEY (axis_id) REFERENCES axis(id) ON DELETE RESTRICT,
    FOREIGN KEY (coord_gt_user_id) REFERENCES "user"(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_gt_project ON gt(project_id);
CREATE INDEX IF NOT EXISTS idx_gt_axis ON gt(axis_id);
CREATE INDEX IF NOT EXISTS idx_gt_coord_user ON gt(coord_gt_user_id);

-- SUBGROUP
CREATE TABLE IF NOT EXISTS subgroup (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id   INTEGER NOT NULL,
    gt_id        INTEGER NOT NULL,
    nome         TEXT NOT NULL,
    descricao    TEXT,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE RESTRICT,
    FOREIGN KEY (gt_id) REFERENCES gt(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_subgroup_gt ON subgroup(gt_id);

-- USER ROLES
CREATE TABLE IF NOT EXISTS user_roles (
    user_id   INTEGER NOT NULL,
    role      TEXT NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- ROLE SCOPES
CREATE TABLE IF NOT EXISTS role_scopes (
    user_id     INTEGER NOT NULL,
    role        TEXT NOT NULL,
    scope_type  TEXT NOT NULL CHECK (scope_type IN ('GLOBAL','AXIS','GT')),
    scope_id    INTEGER,
    PRIMARY KEY (user_id, role, scope_type, scope_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_role_scopes_type ON role_scopes(scope_type);

-- USER PERMISSIONS
CREATE TABLE IF NOT EXISTS user_permissions (
    user_id      INTEGER NOT NULL,
    permission   TEXT NOT NULL,
    scope_type   TEXT NOT NULL CHECK (scope_type IN ('GLOBAL','AXIS','GT')),
    scope_id     INTEGER,
    valid_from   TEXT,
    valid_until  TEXT,
    granted_by   INTEGER,
    granted_at   TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (user_id, permission, scope_type, scope_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES "user"(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_user_permissions_perm ON user_permissions(permission);

-- USER-GT MEMBERSHIP
CREATE TABLE IF NOT EXISTS user_gt (
    user_id     INTEGER NOT NULL,
    gt_id       INTEGER NOT NULL,
    role_in_gt  TEXT NOT NULL,
    PRIMARY KEY (user_id, gt_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (gt_id) REFERENCES gt(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_gt_gt ON user_gt(gt_id);
CREATE INDEX IF NOT EXISTS idx_user_gt_role ON user_gt(role_in_gt);

-- USER-SUBGROUP MEMBERSHIP
CREATE TABLE IF NOT EXISTS user_subgroup (
    user_id      INTEGER NOT NULL,
    subgroup_id  INTEGER NOT NULL,
    PRIMARY KEY (user_id, subgroup_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (subgroup_id) REFERENCES subgroup(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_subgroup_subgroup ON user_subgroup(subgroup_id);

-- ACTIVITY
CREATE TABLE IF NOT EXISTS activity (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id       INTEGER NOT NULL,
    gt_id            INTEGER NOT NULL,
    subgroup_id      INTEGER,
    tipo             TEXT NOT NULL CHECK (tipo IN ('coletiva','individual')),
    titulo           TEXT NOT NULL,
    data             TEXT NOT NULL, -- YYYY-MM-DD
    inicio           TEXT NOT NULL, -- HH:MM
    fim              TEXT NOT NULL, -- HH:MM
    local            TEXT,
    descricao        TEXT,
    evidencias_json  TEXT,
    created_by       INTEGER NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE RESTRICT,
    FOREIGN KEY (gt_id) REFERENCES gt(id) ON DELETE CASCADE,
    FOREIGN KEY (subgroup_id) REFERENCES subgroup(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES "user"(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_activity_gt ON activity(gt_id);
CREATE INDEX IF NOT EXISTS idx_activity_project ON activity(project_id);
CREATE INDEX IF NOT EXISTS idx_activity_date ON activity(data);

-- ACTIVITY TARGET
CREATE TABLE IF NOT EXISTS activity_target (
    activity_id  INTEGER PRIMARY KEY,
    target_type  TEXT NOT NULL CHECK (target_type IN ('ALL_GT','BY_ROLE','BY_LIST')),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE
);

-- ACTIVITY TARGET ROLES
CREATE TABLE IF NOT EXISTS activity_target_role (
    activity_id  INTEGER NOT NULL,
    role         TEXT NOT NULL,
    PRIMARY KEY (activity_id, role),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE
);

-- ACTIVITY PARTICIPANTS (explicit list when BY_LIST)
CREATE TABLE IF NOT EXISTS activity_participant (
    activity_id  INTEGER NOT NULL,
    user_id      INTEGER NOT NULL,
    PRIMARY KEY (activity_id, user_id),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_activity_participant_user ON activity_participant(user_id);

-- ATTENDANCE
CREATE TABLE IF NOT EXISTS attendance (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    activity_id  INTEGER NOT NULL,
    user_id      INTEGER NOT NULL,
    status       TEXT NOT NULL CHECK (status IN ('pendente','validada','rejeitada')),
    modo         TEXT NOT NULL CHECK (modo IN ('auto','manual')),
    created_at   TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_activity_user ON attendance(activity_id, user_id);
CREATE INDEX IF NOT EXISTS idx_attendance_user ON attendance(user_id);

-- ATTENDANCE VALIDATION
CREATE TABLE IF NOT EXISTS attendance_validation (
    attendance_id     INTEGER PRIMARY KEY,
    validator_user_id INTEGER NOT NULL,
    decision          TEXT NOT NULL CHECK (decision IN ('validar','rejeitar')),
    validated_at      TEXT NOT NULL DEFAULT (datetime('now')),
    note              TEXT,
    FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    FOREIGN KEY (validator_user_id) REFERENCES "user"(id) ON DELETE RESTRICT
);

-- SYSTEM SETTINGS
CREATE TABLE IF NOT EXISTS system_settings (
    project_id     INTEGER,
    key            TEXT NOT NULL,
    value          TEXT NOT NULL,
    effective_from TEXT,
    effective_to   TEXT,
    scope_type     TEXT CHECK (scope_type IN ('GLOBAL','AXIS','GT')),
    scope_id       INTEGER,
    PRIMARY KEY (project_id, key, scope_type, scope_id, effective_from),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_system_settings_key ON system_settings(key);

-- AUDIT LOG
CREATE TABLE IF NOT EXISTS audit_log (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_id     INTEGER,
    action       TEXT NOT NULL,
    entity       TEXT NOT NULL,
    entity_id    INTEGER,
    payload_diff TEXT,
    created_at   TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (actor_id) REFERENCES "user"(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);

-- EXPORT LOG
CREATE TABLE IF NOT EXISTS export_log (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_id       INTEGER,
    scope_type     TEXT CHECK (scope_type IN ('GLOBAL','AXIS','GT')),
    scope_id       INTEGER,
    filters_json   TEXT,
    schema_version TEXT NOT NULL,
    created_at     TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (actor_id) REFERENCES "user"(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_export_log_created_at ON export_log(created_at);


-- uStudent · esquema inicial: identidad, acceso y soporte del sistema.
-- Ver docs/02-arquitectura/modelo-datos.md

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ── Identidad ────────────────────────────────────────────────────────

CREATE TABLE iam_users (
    id                BIGSERIAL     PRIMARY KEY,
    public_id         UUID          NOT NULL DEFAULT gen_random_uuid(),
    email             CITEXT        NOT NULL,
    document_number   VARCHAR(20)   NOT NULL,
    full_name         VARCHAR(160)  NOT NULL,
    password_hash     VARCHAR(72)   NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    failed_attempts   SMALLINT      NOT NULL DEFAULT 0,
    locked_until      TIMESTAMPTZ,
    last_login_at     TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_public_id UNIQUE (public_id),
    CONSTRAINT uk_users_email     UNIQUE (email),
    CONSTRAINT uk_users_document  UNIQUE (document_number),
    CONSTRAINT ck_users_status    CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE TABLE iam_roles (
    id           BIGSERIAL     PRIMARY KEY,
    public_id    UUID          NOT NULL DEFAULT gen_random_uuid(),
    code         VARCHAR(40)   NOT NULL,
    name         VARCHAR(80)   NOT NULL,
    description  VARCHAR(255),
    -- Protege los roles predefinidos: no se pueden eliminar desde la aplicacion.
    is_system    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_roles_public_id UNIQUE (public_id),
    CONSTRAINT uk_roles_code      UNIQUE (code)
);

CREATE TABLE iam_permissions (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(60)  NOT NULL,
    resource    VARCHAR(30)  NOT NULL,
    action      VARCHAR(30)  NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE TABLE iam_user_roles (
    user_id     BIGINT       NOT NULL REFERENCES iam_users (id) ON DELETE CASCADE,
    role_id     BIGINT       NOT NULL REFERENCES iam_roles (id) ON DELETE RESTRICT,
    assigned_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE iam_role_permissions (
    role_id       BIGINT NOT NULL REFERENCES iam_roles (id)       ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES iam_permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Rotacion con deteccion de reutilizacion (ADR-0005): usar un token ya
-- consumido invalida toda la familia.
CREATE TABLE iam_refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES iam_users (id) ON DELETE CASCADE,
    jti         UUID         NOT NULL,
    family      UUID         NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMPTZ,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_refresh_jti UNIQUE (jti)
);

CREATE INDEX ix_refresh_user    ON iam_refresh_tokens (user_id, revoked);
CREATE INDEX ix_refresh_expires ON iam_refresh_tokens (expires_at);

-- ── Soporte del sistema ──────────────────────────────────────────────

-- Bitacora append-only. El usuario de la aplicacion recibe INSERT y SELECT,
-- nunca UPDATE ni DELETE (ver docs/02-arquitectura/seguridad.md).
CREATE TABLE sys_audit_log (
    id             BIGSERIAL    PRIMARY KEY,
    actor_user_id  BIGINT       REFERENCES iam_users (id) ON DELETE SET NULL,
    action         VARCHAR(60)  NOT NULL,
    resource_type  VARCHAR(40)  NOT NULL,
    resource_id    VARCHAR(64),
    metadata       JSONB,
    ip_address     INET,
    user_agent     VARCHAR(255),
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_audit_actor    ON sys_audit_log (actor_user_id, occurred_at DESC);
CREATE INDEX ix_audit_resource ON sys_audit_log (resource_type, resource_id);
CREATE INDEX ix_audit_occurred ON sys_audit_log (occurred_at DESC);

-- Sustituye a un broker de mensajes en la v1 (ver ADR-0001).
CREATE TABLE sys_async_jobs (
    id          BIGSERIAL    PRIMARY KEY,
    type        VARCHAR(40)  NOT NULL,
    payload     JSONB        NOT NULL,
    status      VARCHAR(12)  NOT NULL DEFAULT 'PENDING',
    attempts    SMALLINT     NOT NULL DEFAULT 0,
    last_error  TEXT,
    run_after   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_jobs_status CHECK (status IN ('PENDING', 'RUNNING', 'DONE', 'FAILED'))
);

CREATE INDEX ix_jobs_pending ON sys_async_jobs (status, run_after)
    WHERE status = 'PENDING';

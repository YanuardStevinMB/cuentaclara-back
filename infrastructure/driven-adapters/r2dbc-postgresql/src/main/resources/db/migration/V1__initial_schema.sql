-- V1: Core identity tables (users, tenants, tenant_memberships)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================
-- USERS
-- =============================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    password_hash   VARCHAR(255) NOT NULL,
    system_role     VARCHAR(20) NOT NULL DEFAULT 'USER'
                        CHECK (system_role IN ('USER', 'SUPPORT', 'ADMIN', 'SUPER_ADMIN')),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED')),
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

-- Email unique for non-deleted users
CREATE UNIQUE INDEX uq_users_email_active ON users (email) WHERE deleted_at IS NULL;

-- =============================================
-- TENANTS
-- =============================================
CREATE TABLE tenants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    tenant_type         VARCHAR(20) NOT NULL DEFAULT 'PERSONAL'
                            CHECK (tenant_type IN ('PERSONAL', 'BUSINESS')),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED', 'DELETED')),
    created_by_user_id  UUID NOT NULL REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- =============================================
-- TENANT MEMBERSHIPS
-- =============================================
CREATE TABLE tenant_memberships (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    tenant_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
                    CHECK (tenant_role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'REMOVED')),
    joined_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_tenant_memberships_tenant_user ON tenant_memberships (tenant_id, user_id);

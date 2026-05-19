-- V2: User support tables (preferences, legal documents, tokens)

-- =============================================
-- USER PREFERENCES
-- =============================================
CREATE TABLE user_preferences (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL UNIQUE REFERENCES users(id),
    language    VARCHAR(10) NOT NULL DEFAULT 'es',
    currency    VARCHAR(10) NOT NULL DEFAULT 'COP',
    theme       VARCHAR(20) NOT NULL DEFAULT 'LIGHT',
    timezone    VARCHAR(50) NOT NULL DEFAULT 'America/Bogota',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- LEGAL DOCUMENT VERSIONS
-- =============================================
CREATE TABLE legal_document_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type   VARCHAR(50) NOT NULL,
    version         VARCHAR(20) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content_url     VARCHAR(500) NOT NULL,
    effective_from  TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_legal_doc_type_version ON legal_document_versions (document_type, version);

-- =============================================
-- USER LEGAL ACCEPTANCES
-- =============================================
CREATE TABLE user_legal_acceptances (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID NOT NULL REFERENCES users(id),
    legal_document_version_id   UUID NOT NULL REFERENCES legal_document_versions(id),
    accepted_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ip_address                  VARCHAR(45),
    user_agent                  VARCHAR(500)
);

CREATE UNIQUE INDEX uq_user_legal_acceptance ON user_legal_acceptances (user_id, legal_document_version_id);

-- =============================================
-- REFRESH TOKENS
-- =============================================
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255) NOT NULL,
    device_info VARCHAR(255),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id, revoked_at);

-- =============================================
-- PASSWORD RESET TOKENS
-- =============================================
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_password_reset_tokens_user ON password_reset_tokens (user_id, used_at);

-- V9: User preferences and privacy consent

CREATE TABLE IF NOT EXISTS user_preferences (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL UNIQUE REFERENCES users(id),
    currency    VARCHAR(10) NOT NULL DEFAULT 'COP',
    date_format VARCHAR(20) NOT NULL DEFAULT 'DD/MM/YYYY',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS privacy_consents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    policy_version  VARCHAR(20) NOT NULL,
    accepted        BOOLEAN NOT NULL DEFAULT TRUE,
    ip_address      VARCHAR(45),
    accepted_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_privacy_consents_user ON privacy_consents (user_id, accepted_at DESC);

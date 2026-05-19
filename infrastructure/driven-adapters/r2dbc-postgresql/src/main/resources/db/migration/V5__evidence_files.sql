-- V5: Evidence files and relationship tables

-- =============================================
-- EVIDENCE FILES
-- =============================================
CREATE TABLE evidence_files (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    uploaded_by_user_id UUID NOT NULL REFERENCES users(id),
    original_filename   VARCHAR(255) NOT NULL,
    storage_key         VARCHAR(500) NOT NULL,
    mime_type           VARCHAR(100) NOT NULL,
    size_bytes          BIGINT NOT NULL,
    sha256_hash         VARCHAR(64),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'DELETED', 'QUARANTINED')),
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- =============================================
-- LOAN EVIDENCES
-- =============================================
CREATE TABLE loan_evidences (
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    loan_id         UUID NOT NULL REFERENCES loans(id),
    evidence_id     UUID NOT NULL REFERENCES evidence_files(id),
    attached_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (loan_id, evidence_id)
);

-- =============================================
-- PAYMENT EVIDENCES
-- =============================================
CREATE TABLE payment_evidences (
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    payment_id      UUID NOT NULL REFERENCES payments(id),
    evidence_id     UUID NOT NULL REFERENCES evidence_files(id),
    attached_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (payment_id, evidence_id)
);

-- =============================================
-- MOVEMENT EVIDENCES
-- =============================================
CREATE TABLE movement_evidences (
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    movement_id     UUID NOT NULL REFERENCES movements(id),
    evidence_id     UUID NOT NULL REFERENCES evidence_files(id),
    attached_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (movement_id, evidence_id)
);

-- V7: Audit logs, support tickets, FAQs, report exports

-- =============================================
-- AUDIT LOGS
-- =============================================
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID REFERENCES tenants(id),
    actor_user_id   UUID NOT NULL REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_name     VARCHAR(100) NOT NULL,
    entity_id       UUID NOT NULL,
    old_values      JSONB,
    new_values      JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- SUPPORT TICKETS
-- =============================================
CREATE TABLE support_tickets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    subject         VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'
                        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ
);

-- Add FK to notifications now that support_tickets exists
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_support_ticket
    FOREIGN KEY (support_ticket_id) REFERENCES support_tickets(id);

-- =============================================
-- SUPPORT MESSAGES
-- =============================================
CREATE TABLE support_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id       UUID NOT NULL REFERENCES support_tickets(id),
    sender_user_id  UUID NOT NULL REFERENCES users(id),
    message         TEXT NOT NULL,
    is_internal     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- FAQS
-- =============================================
CREATE TABLE faqs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category    VARCHAR(100),
    question    VARCHAR(500) NOT NULL,
    answer      TEXT NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- REPORT EXPORTS
-- =============================================
CREATE TABLE report_exports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    report_type     VARCHAR(50) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    storage_key     VARCHAR(500) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);

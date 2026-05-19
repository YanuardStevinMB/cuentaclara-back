-- V6: Budgets, notification preferences, reminders, notifications

-- =============================================
-- BUDGETS
-- =============================================
CREATE TABLE budgets (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL REFERENCES tenants(id),
    category_id             UUID NOT NULL REFERENCES categories(id),
    created_by_user_id      UUID NOT NULL REFERENCES users(id),
    period_start            DATE NOT NULL,
    period_end              DATE NOT NULL,
    limit_amount            DECIMAL(15,2) NOT NULL CHECK (limit_amount > 0),
    alert_threshold_percent INTEGER NOT NULL DEFAULT 80 CHECK (alert_threshold_percent BETWEEN 1 AND 100),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ
);

-- One active budget per tenant, category and period
CREATE UNIQUE INDEX uq_budgets_tenant_category_period
    ON budgets (tenant_id, category_id, period_start, period_end)
    WHERE active = TRUE AND deleted_at IS NULL;

-- =============================================
-- NOTIFICATION PREFERENCES
-- =============================================
CREATE TABLE notification_preferences (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    channel         VARCHAR(20) NOT NULL DEFAULT 'IN_APP'
                        CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH')),
    loan_reminders  BOOLEAN NOT NULL DEFAULT TRUE,
    budget_alerts   BOOLEAN NOT NULL DEFAULT TRUE,
    payment_alerts  BOOLEAN NOT NULL DEFAULT TRUE,
    system_alerts   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_notification_prefs_tenant_user ON notification_preferences (tenant_id, user_id);

-- =============================================
-- REMINDERS
-- =============================================
CREATE TABLE reminders (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    loan_id     UUID REFERENCES loans(id),
    budget_id   UUID REFERENCES budgets(id),
    payment_id  UUID REFERENCES payments(id),
    source      VARCHAR(20) NOT NULL DEFAULT 'MANUAL'
                    CHECK (source IN ('MANUAL', 'AUTOMATIC')),
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'SENT', 'CANCELLED', 'DONE')),
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    remind_at   TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- NOTIFICATIONS
-- =============================================
CREATE TABLE notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    reminder_id         UUID REFERENCES reminders(id),
    loan_id             UUID REFERENCES loans(id),
    budget_id           UUID REFERENCES budgets(id),
    support_ticket_id   UUID,
    notification_type   VARCHAR(50) NOT NULL,
    title               VARCHAR(255) NOT NULL,
    message             VARCHAR(1000),
    read_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

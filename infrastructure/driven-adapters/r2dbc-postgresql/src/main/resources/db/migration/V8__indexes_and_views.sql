-- V8: Performance indexes and recommended views

-- =============================================
-- PERFORMANCE INDEXES
-- =============================================
CREATE INDEX ix_movements_tenant_date ON movements (tenant_id, occurred_on DESC);
CREATE INDEX ix_movements_tenant_type_date ON movements (tenant_id, movement_type, occurred_on DESC);
CREATE INDEX ix_loans_tenant_status_due ON loans (tenant_id, status, due_date);
CREATE INDEX ix_payments_tenant_loan_date ON payments (tenant_id, loan_id, paid_on DESC);
CREATE INDEX ix_contacts_tenant_name ON contacts (tenant_id, full_name);
CREATE INDEX ix_notifications_user_read ON notifications (tenant_id, user_id, read_at, created_at DESC);
CREATE INDEX ix_audit_logs_tenant_entity ON audit_logs (tenant_id, entity_name, entity_id, created_at DESC);
CREATE INDEX ix_reminders_tenant_status ON reminders (tenant_id, status, remind_at);
CREATE INDEX ix_evidence_files_tenant ON evidence_files (tenant_id, status);
CREATE INDEX ix_budgets_tenant_category ON budgets (tenant_id, category_id, period_start);

-- =============================================
-- VIEW: loan_balances
-- =============================================
CREATE OR REPLACE VIEW loan_balances AS
SELECT
    l.id AS loan_id,
    l.tenant_id,
    l.contact_id,
    l.loan_direction,
    l.status,
    l.principal_amount,
    l.expected_total_amount,
    COALESCE(SUM(p.amount) FILTER (WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL), 0) AS total_paid,
    l.expected_total_amount - COALESCE(SUM(p.amount) FILTER (WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL), 0) AS outstanding_amount
FROM loans l
LEFT JOIN payments p ON p.loan_id = l.id
WHERE l.deleted_at IS NULL
GROUP BY l.id, l.tenant_id, l.contact_id, l.loan_direction, l.status,
         l.principal_amount, l.expected_total_amount;

-- =============================================
-- VIEW: monthly_movement_summary
-- =============================================
CREATE OR REPLACE VIEW monthly_movement_summary AS
SELECT
    m.tenant_id,
    DATE_TRUNC('month', m.occurred_on) AS period_month,
    m.movement_type,
    COUNT(*) AS movement_count,
    SUM(m.amount) AS total_amount
FROM movements m
WHERE m.status = 'ACTIVE'
  AND m.deleted_at IS NULL
GROUP BY m.tenant_id, DATE_TRUNC('month', m.occurred_on), m.movement_type;

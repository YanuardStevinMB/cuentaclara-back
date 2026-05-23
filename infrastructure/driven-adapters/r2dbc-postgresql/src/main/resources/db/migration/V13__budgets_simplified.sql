-- V13: Budgets simplified (user_id based, no tenant)

DROP TABLE IF EXISTS budgets CASCADE;

CREATE TABLE budgets (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id),
    category_id             UUID NOT NULL REFERENCES categories(id),
    period_month            INTEGER NOT NULL CHECK (period_month BETWEEN 1 AND 12),
    period_year             INTEGER NOT NULL CHECK (period_year >= 2020),
    limit_amount            DECIMAL(15,2) NOT NULL CHECK (limit_amount > 0),
    alert_threshold_percent INTEGER NOT NULL DEFAULT 80 CHECK (alert_threshold_percent BETWEEN 1 AND 100),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ
);

-- One active budget per user, category and month
CREATE UNIQUE INDEX uq_budgets_user_category_period
    ON budgets (user_id, category_id, period_month, period_year)
    WHERE active = TRUE AND deleted_at IS NULL;

CREATE INDEX ix_budgets_user ON budgets (user_id, period_year, period_month) WHERE deleted_at IS NULL;

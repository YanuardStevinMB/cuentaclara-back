-- V10: Categories, movements, and scheduled payments for dashboard
-- This migration is designed to be compatible with existing schema from V3

-- Enhance categories table if needed (existing table from V3 uses tenant_id instead of user_id)
-- Add missing columns only if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'categories' AND column_name = 'user_id') THEN
        ALTER TABLE categories ADD COLUMN user_id UUID REFERENCES users(id);
    END IF;
END $$;

-- Create movements table only if it doesn't exist (V3 creates it with different structure)
CREATE TABLE IF NOT EXISTS movements_v10 (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id),
    category_id   UUID REFERENCES categories(id),
    type          VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount        NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    description   VARCHAR(255),
    movement_date DATE NOT NULL,
    scope         VARCHAR(10) NOT NULL DEFAULT 'PERSONAL' CHECK (scope IN ('PERSONAL', 'BUSINESS')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS ix_movements_v10_user_date ON movements_v10 (user_id, movement_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_movements_v10_user_type ON movements_v10 (user_id, type, movement_date) WHERE deleted_at IS NULL;

-- Create scheduled_payments table
CREATE TABLE IF NOT EXISTS scheduled_payments (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id),
    name          VARCHAR(150) NOT NULL,
    amount        NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    due_date      DATE NOT NULL,
    category_id   UUID REFERENCES categories(id),
    recurring     BOOLEAN NOT NULL DEFAULT FALSE,
    status        VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_scheduled_payments_user ON scheduled_payments (user_id, due_date) WHERE status != 'PAID';

-- V10: Categories, movements, and scheduled payments for dashboard

CREATE TABLE IF NOT EXISTS categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id),
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    icon        VARCHAR(50),
    color       VARCHAR(7),
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Only run if categories table has user_id column (skipped when V3 schema is present instead)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'categories' AND column_name = 'user_id'
    ) THEN
        CREATE INDEX IF NOT EXISTS ix_categories_user ON categories (user_id, type, active);

        INSERT INTO categories (id, user_id, name, type, icon, color, is_system, active) VALUES
            ('a0000000-0000-0000-0000-000000000001', NULL, 'Salario', 'INCOME', '💰', '#10b981', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000002', NULL, 'Freelance', 'INCOME', '💻', '#3b82f6', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000003', NULL, 'Inversiones', 'INCOME', '📈', '#8b5cf6', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000004', NULL, 'Otros ingresos', 'INCOME', '💵', '#6b7280', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000010', NULL, 'Vivienda', 'EXPENSE', '🏠', '#ef4444', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000011', NULL, 'Alimentos', 'EXPENSE', '🍔', '#f59e0b', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000012', NULL, 'Transporte', 'EXPENSE', '🚗', '#3b82f6', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000013', NULL, 'Servicios', 'EXPENSE', '⚡', '#8b5cf6', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000014', NULL, 'Entretenimiento', 'EXPENSE', '🎬', '#ec4899', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000015', NULL, 'Salud', 'EXPENSE', '🏥', '#14b8a6', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000016', NULL, 'Educación', 'EXPENSE', '📚', '#6366f1', TRUE, TRUE),
            ('a0000000-0000-0000-0000-000000000017', NULL, 'Otros gastos', 'EXPENSE', '📦', '#6b7280', TRUE, TRUE)
        ON CONFLICT (id) DO NOTHING;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS movements (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id),
    category_id   UUID NOT NULL REFERENCES categories(id),
    type          VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount        NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    description   VARCHAR(255),
    movement_date DATE NOT NULL,
    scope         VARCHAR(10) NOT NULL DEFAULT 'PERSONAL' CHECK (scope IN ('PERSONAL', 'BUSINESS')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

-- Only run if movements table has user_id column (skipped when V3 schema is present instead)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'movements' AND column_name = 'user_id'
    ) THEN
        CREATE INDEX IF NOT EXISTS ix_movements_user_date ON movements (user_id, movement_date DESC) WHERE deleted_at IS NULL;
        CREATE INDEX IF NOT EXISTS ix_movements_user_type ON movements (user_id, type, movement_date) WHERE deleted_at IS NULL;
    END IF;
END $$;

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

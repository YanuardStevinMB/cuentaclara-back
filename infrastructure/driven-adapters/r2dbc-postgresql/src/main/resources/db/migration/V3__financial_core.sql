-- V3: Financial core (categories, movements, contacts)

-- =============================================
-- CATEGORIES
-- =============================================
CREATE TABLE categories (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    parent_category_id  UUID REFERENCES categories(id),
    name                VARCHAR(100) NOT NULL,
    category_kind       VARCHAR(20) NOT NULL DEFAULT 'BOTH'
                            CHECK (category_kind IN ('INCOME', 'EXPENSE', 'BOTH')),
    financial_scope     VARCHAR(20) NOT NULL DEFAULT 'BOTH'
                            CHECK (financial_scope IN ('PERSONAL', 'BUSINESS', 'BOTH')),
    color               VARCHAR(7),
    icon                VARCHAR(50),
    is_system           BOOLEAN NOT NULL DEFAULT FALSE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_user_id  UUID NOT NULL REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX ix_categories_tenant ON categories (tenant_id, active);

-- =============================================
-- MOVEMENTS
-- =============================================
CREATE TABLE movements (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    created_by_user_id  UUID NOT NULL REFERENCES users(id),
    category_id         UUID REFERENCES categories(id),
    movement_type       VARCHAR(20) NOT NULL
                            CHECK (movement_type IN ('INCOME', 'EXPENSE')),
    financial_scope     VARCHAR(20) NOT NULL DEFAULT 'PERSONAL'
                            CHECK (financial_scope IN ('PERSONAL', 'BUSINESS')),
    amount              DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    occurred_on         DATE NOT NULL,
    description         VARCHAR(500),
    payment_method      VARCHAR(50),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'VOIDED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- =============================================
-- CONTACTS
-- =============================================
CREATE TABLE contacts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    created_by_user_id  UUID NOT NULL REFERENCES users(id),
    full_name           VARCHAR(255) NOT NULL,
    phone               VARCHAR(50),
    email               VARCHAR(255),
    document_number     VARCHAR(50),
    notes               VARCHAR(500),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

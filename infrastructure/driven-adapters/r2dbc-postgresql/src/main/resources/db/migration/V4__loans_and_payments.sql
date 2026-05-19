-- V4: Loans and payments

-- =============================================
-- LOANS
-- =============================================
CREATE TABLE loans (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   UUID NOT NULL REFERENCES tenants(id),
    contact_id                  UUID NOT NULL REFERENCES contacts(id),
    created_by_user_id          UUID NOT NULL REFERENCES users(id),
    loan_direction              VARCHAR(20) NOT NULL
                                    CHECK (loan_direction IN ('GIVEN', 'RECEIVED')),
    status                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                    CHECK (status IN ('ACTIVE', 'OVERDUE', 'PAID', 'CANCELLED', 'ARCHIVED')),
    title                       VARCHAR(255) NOT NULL,
    principal_amount            DECIMAL(15,2) NOT NULL CHECK (principal_amount > 0),
    interest_type               VARCHAR(20) NOT NULL DEFAULT 'NONE'
                                    CHECK (interest_type IN ('NONE', 'FIXED', 'PERCENTAGE')),
    interest_rate               DECIMAL(8,4),
    interest_amount             DECIMAL(15,2),
    expected_total_amount       DECIMAL(15,2) NOT NULL,
    loan_date                   DATE NOT NULL,
    due_date                    DATE,
    description                 VARCHAR(500),
    legal_disclaimer_accepted   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at                  TIMESTAMPTZ
);

-- =============================================
-- PAYMENTS
-- =============================================
CREATE TABLE payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL REFERENCES tenants(id),
    loan_id                 UUID NOT NULL REFERENCES loans(id),
    registered_by_user_id   UUID NOT NULL REFERENCES users(id),
    amount                  DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    paid_on                 DATE NOT NULL,
    payment_method          VARCHAR(50),
    description             VARCHAR(500),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE', 'VOIDED')),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ
);

-- V12: Recreate contacts table with simplified schema (user_id based, no tenant)

DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS contacts CASCADE;

CREATE TABLE contacts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by_user_id  UUID NOT NULL REFERENCES users(id),
    full_name           VARCHAR(255) NOT NULL,
    phone               VARCHAR(50) DEFAULT '',
    email               VARCHAR(255) DEFAULT '',
    notes               VARCHAR(500) DEFAULT '',
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX ix_contacts_user ON contacts (created_by_user_id, active) WHERE deleted_at IS NULL;
CREATE INDEX ix_contacts_name ON contacts (created_by_user_id, LOWER(full_name)) WHERE deleted_at IS NULL;

-- Recreate loans table referencing contacts
CREATE TABLE loans (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id                  UUID NOT NULL REFERENCES contacts(id),
    created_by_user_id          UUID NOT NULL REFERENCES users(id),
    loan_direction              VARCHAR(20) NOT NULL CHECK (loan_direction IN ('GIVEN', 'RECEIVED')),
    status                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                    CHECK (status IN ('ACTIVE', 'OVERDUE', 'PAID', 'CANCELLED')),
    title                       VARCHAR(255) NOT NULL,
    principal_amount            DECIMAL(15,2) NOT NULL CHECK (principal_amount > 0),
    interest_type               VARCHAR(20) NOT NULL DEFAULT 'NONE'
                                    CHECK (interest_type IN ('NONE', 'FIXED', 'PERCENTAGE')),
    interest_rate               DECIMAL(8,4),
    expected_total_amount       DECIMAL(15,2) NOT NULL,
    loan_date                   DATE NOT NULL,
    due_date                    DATE,
    description                 VARCHAR(500),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at                  TIMESTAMPTZ
);

CREATE INDEX ix_loans_contact ON loans (contact_id, status) WHERE deleted_at IS NULL;
CREATE INDEX ix_loans_user ON loans (created_by_user_id, status) WHERE deleted_at IS NULL;

CREATE TABLE payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE INDEX ix_payments_loan ON payments (loan_id, status) WHERE deleted_at IS NULL;

-- seed_default_categories.sql
-- Creates a system seed user, tenant, and default categories.
-- Safe to run multiple times (ON CONFLICT DO NOTHING).

DO $$
DECLARE
    v_user_id   UUID := 'b0000000-0000-0000-0000-000000000001';
    v_tenant_id UUID := 'c0000000-0000-0000-0000-000000000001';
BEGIN
    -- System seed user
    INSERT INTO users (id, email, full_name, password_hash, system_role, status)
    VALUES (
        v_user_id,
        'system@cuentaclara.internal',
        'Sistema',
        '$2a$10$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.',
        'ADMIN',
        'INACTIVE'
    )
    ON CONFLICT (id) DO NOTHING;

    -- System seed tenant
    INSERT INTO tenants (id, name, tenant_type, status, created_by_user_id)
    VALUES (v_tenant_id, 'Sistema', 'PERSONAL', 'ACTIVE', v_user_id)
    ON CONFLICT (id) DO NOTHING;

    -- Default categories
    INSERT INTO categories (id, tenant_id, name, category_kind, color, icon, is_system, active, created_by_user_id)
    VALUES
        ('a0000000-0000-0000-0000-000000000001', v_tenant_id, 'Salario',        'INCOME',  '#10b981', '💰', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000002', v_tenant_id, 'Freelance',      'INCOME',  '#3b82f6', '💻', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000003', v_tenant_id, 'Inversiones',    'INCOME',  '#8b5cf6', '📈', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000004', v_tenant_id, 'Otros ingresos', 'INCOME',  '#6b7280', '💵', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000010', v_tenant_id, 'Vivienda',       'EXPENSE', '#ef4444', '🏠', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000011', v_tenant_id, 'Alimentos',      'EXPENSE', '#f59e0b', '🍔', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000012', v_tenant_id, 'Transporte',     'EXPENSE', '#3b82f6', '🚗', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000013', v_tenant_id, 'Servicios',      'EXPENSE', '#8b5cf6', '⚡', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000014', v_tenant_id, 'Entretenimiento','EXPENSE', '#ec4899', '🎬', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000015', v_tenant_id, 'Salud',          'EXPENSE', '#14b8a6', '🏥', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000016', v_tenant_id, 'Educación',      'EXPENSE', '#6366f1', '📚', TRUE, TRUE, v_user_id),
        ('a0000000-0000-0000-0000-000000000017', v_tenant_id, 'Otros gastos',   'EXPENSE', '#6b7280', '📦', TRUE, TRUE, v_user_id)
    ON CONFLICT (id) DO NOTHING;

    RAISE NOTICE 'Done. Categories inserted.';
END $$;

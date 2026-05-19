-- V11: Add payment_method column to movements
ALTER TABLE movements ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);

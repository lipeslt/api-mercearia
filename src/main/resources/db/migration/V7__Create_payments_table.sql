-- Payments table linked to tb_pedidos (UUID)

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    pedido_id UUID NOT NULL,
    mercado_pago_id VARCHAR(255),
    amount NUMERIC(38,2),
    payment_method VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- status check (mesmo do dump)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'payments_status_check'
    ) THEN
        ALTER TABLE payments
            ADD CONSTRAINT payments_status_check
            CHECK (status::text = ANY (ARRAY[
                'PENDING',
                'APPROVED',
                'REJECTED',
                'CANCELLED',
                'REFUNDED'
            ]::text[]));
    END IF;
END$$;

-- Add Foreign Key with safe existence check
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_payments_pedido'
    ) THEN
        ALTER TABLE payments
            ADD CONSTRAINT fk_payments_pedido
            FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id) ON DELETE CASCADE;
    END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_payments_pedido_id ON payments(pedido_id);
CREATE INDEX IF NOT EXISTS idx_payments_mercado_pago_id ON payments(mercado_pago_id);
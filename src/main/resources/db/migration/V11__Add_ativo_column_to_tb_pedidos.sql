-- ===============================
-- V11: Add ativo column to tb_pedidos
-- ===============================
-- Adiciona coluna 'ativo' em tb_pedidos para manter compatibilidade com BaseEntity

ALTER TABLE tb_pedidos ADD COLUMN ativo BOOLEAN DEFAULT TRUE NOT NULL;

-- Criar índice para melhorar performance em queries que filtram por ativo
CREATE INDEX idx_tb_pedidos_ativo ON tb_pedidos(ativo);

-- ===============================
-- End of migration
-- ===============================
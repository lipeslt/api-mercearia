-- ===============================
-- V10: Add ativo column to tb_itens_pedido
-- ===============================
-- Adiciona coluna 'ativo' em tb_itens_pedido para manter compatibilidade com BaseEntity

ALTER TABLE tb_itens_pedido ADD COLUMN ativo BOOLEAN DEFAULT TRUE NOT NULL;

-- Criar índice para melhorar performance em queries que filtram por ativo
CREATE INDEX idx_tb_itens_pedido_ativo ON tb_itens_pedido(ativo);

-- ===============================
-- End of migration
-- ===============================
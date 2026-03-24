-- Create modern "tb_*" schema using UUIDs + audit fields
-- PostgreSQL requirement: pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ===============================
-- tb_usuarios
-- ===============================
CREATE TABLE IF NOT EXISTS tb_usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    foto_perfil VARCHAR(500),
    nome VARCHAR(100) NOT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    senha VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_usuarios_email ON tb_usuarios(email);

-- ===============================
-- tb_categorias
-- ===============================
CREATE TABLE IF NOT EXISTS tb_categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descricao TEXT,
    nome VARCHAR(100) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_por UUID,
    ativo BOOLEAN NOT NULL DEFAULT true
);

ALTER TABLE tb_categorias
    ADD CONSTRAINT IF NOT EXISTS uk_tb_categorias_nome UNIQUE (nome);

-- ===============================
-- tb_produtos
-- ===============================
CREATE TABLE IF NOT EXISTS tb_produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estoque INTEGER NOT NULL CHECK (estoque >= 0),
    nome VARCHAR(150) NOT NULL,
    preco NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    categoria_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_por UUID,
    ativo BOOLEAN NOT NULL DEFAULT true
);

ALTER TABLE tb_produtos
    ADD CONSTRAINT IF NOT EXISTS fk_tb_produtos_categoria
    FOREIGN KEY (categoria_id) REFERENCES tb_categorias(id);

CREATE INDEX IF NOT EXISTS idx_tb_produtos_categoria_id ON tb_produtos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_tb_produtos_ativo ON tb_produtos(ativo);

-- ===============================
-- tb_pedidos
-- ===============================
CREATE TABLE IF NOT EXISTS tb_pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) NOT NULL,
    valor_total NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (valor_total >= 0),
    usuario_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_por UUID
);

-- status check (mesmo do dump)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'tb_pedidos_status_check'
    ) THEN
        ALTER TABLE tb_pedidos
            ADD CONSTRAINT tb_pedidos_status_check
            CHECK (status::text = ANY (ARRAY[
                'AGUARDANDO_PAGAMENTO',
                'PAGO',
                'SEPARACAO_ESTOQUE',
                'ENVIADO',
                'ENTREGUE',
                'CANCELADO'
            ]::text[]));
    END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_tb_pedidos_usuario_id ON tb_pedidos(usuario_id);

-- ===============================
-- tb_itens_pedido
-- ===============================
CREATE TABLE IF NOT EXISTS tb_itens_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    preco_unitario NUMERIC(10,2) NOT NULL CHECK (preco_unitario >= 0),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    pedido_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE tb_itens_pedido
    ADD CONSTRAINT IF NOT EXISTS fk_tb_itens_pedido_pedido
    FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id);

ALTER TABLE tb_itens_pedido
    ADD CONSTRAINT IF NOT EXISTS fk_tb_itens_pedido_produto
    FOREIGN KEY (produto_id) REFERENCES tb_produtos(id);

CREATE INDEX IF NOT EXISTS idx_tb_itens_pedido_pedido_id ON tb_itens_pedido(pedido_id);
CREATE INDEX IF NOT EXISTS idx_tb_itens_pedido_produto_id ON tb_itens_pedido(produto_id);

-- ===============================
-- tb_carrossel (equivalente ao "imagem_carrossel" legado)
-- ===============================
CREATE TABLE IF NOT EXISTS tb_carrossel (
    ordem_exibicao INTEGER NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo_opcional VARCHAR(100),
    image_url VARCHAR(500) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tb_carrossel_ordem_exibicao ON tb_carrossel(ordem_exibicao);
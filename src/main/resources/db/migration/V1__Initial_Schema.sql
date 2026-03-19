-- Usuario Table
CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Categoria Table
CREATE TABLE IF NOT EXISTS categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Produto Table
CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    estoque INT NOT NULL,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Pedido Table
CREATE TABLE IF NOT EXISTS pedido (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ItemPedido Table
CREATE TABLE IF NOT EXISTS item_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id),
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ImagemCarrossel Table
CREATE TABLE IF NOT EXISTS imagem_carrossel (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255),
    descricao TEXT,
    url_imagem VARCHAR(255),
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_produto_categoria ON produto(categoria_id);
CREATE INDEX idx_pedido_usuario ON pedido(usuario_id);
CREATE INDEX idx_item_pedido_pedido ON item_pedido(pedido_id);
CREATE INDEX idx_item_pedido_produto ON item_pedido(produto_id);
-- Tabela para controlar tentativas de login
CREATE TABLE IF NOT EXISTS tentativa_login (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    tentativas INTEGER DEFAULT 0,
    ultima_tentativa TIMESTAMP,
    bloqueado BOOLEAN DEFAULT false,
    data_bloqueio TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_tentativa_login_email ON tentativa_login(email);
CREATE INDEX IF NOT EXISTS idx_tentativa_login_bloqueado ON tentativa_login(bloqueado);
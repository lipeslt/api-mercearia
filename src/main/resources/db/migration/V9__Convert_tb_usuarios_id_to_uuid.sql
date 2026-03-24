-- Converte coluna id em tb_usuarios de BIGSERIAL para UUID

-- Passo 1: Remover constraints existentes
ALTER TABLE tb_usuarios DROP CONSTRAINT IF EXISTS tb_usuarios_email_key;
ALTER TABLE tb_usuarios DROP CONSTRAINT IF EXISTS tb_usuarios_pkey;

-- Passo 2: Criar coluna temporária UUID
ALTER TABLE tb_usuarios ADD COLUMN id_uuid UUID DEFAULT gen_random_uuid();

-- Passo 3: Atualizar a coluna temporária com valores únicos
UPDATE tb_usuarios SET id_uuid = gen_random_uuid() WHERE id_uuid IS NULL;

-- Passo 4: Remover coluna id antiga
ALTER TABLE tb_usuarios DROP COLUMN id;

-- Passo 5: Renomear coluna temporária para id
ALTER TABLE tb_usuarios RENAME COLUMN id_uuid TO id;

-- Passo 6: Adicionar constraint PRIMARY KEY
ALTER TABLE tb_usuarios ADD CONSTRAINT tb_usuarios_pkey PRIMARY KEY (id);

-- Passo 7: Adicionar constraint UNIQUE em email
ALTER TABLE tb_usuarios ADD CONSTRAINT tb_usuarios_email_key UNIQUE (email);
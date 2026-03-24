-- Adiciona coluna descricao para tabela tb_produtos se não existir
ALTER TABLE tb_produtos
ADD COLUMN descricao TEXT;
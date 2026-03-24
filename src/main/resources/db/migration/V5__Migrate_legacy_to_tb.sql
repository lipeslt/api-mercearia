-- Best-effort migration from legacy tables to modern tb_* tables.
-- Safe to run on empty or partial DBs (all inserts are conditional).

-- Usuarios
INSERT INTO usuario (nome, email, senha, ativo, created_at, updated_at)
SELECT u.nome, u.email, u.senha,
       COALESCE(u.ativo, true),
       COALESCE(u.created_at, CURRENT_TIMESTAMP),
       COALESCE(u.updated_at, CURRENT_TIMESTAMP)
FROM usuario u
WHERE FALSE; -- placeholder: legacy table is same as current

-- Categorias
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'categoria' AND table_schema = 'public') THEN
    -- already the same table, no migration needed
    NULL;
  END IF;
END $$;

-- Carrossel: migrate from imagem_carrossel if it exists
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'imagem_carrossel' AND table_schema = 'public') THEN
    INSERT INTO tb_carrossel (ordem_exibicao, titulo_opcional, image_url)
    SELECT
        ROW_NUMBER() OVER (ORDER BY ic.id)::integer,
        ic.titulo,
        ic.url_imagem
    FROM imagem_carrossel ic
    WHERE ic.url_imagem IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM tb_carrossel tc WHERE tc.image_url = ic.url_imagem
      );
  END IF;
END $$;
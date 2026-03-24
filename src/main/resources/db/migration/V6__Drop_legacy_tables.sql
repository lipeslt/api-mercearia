-- Drop legacy schema after migration.
-- Order matters because of FKs.

DROP TABLE IF EXISTS item_pedido;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS imagem_carrossel;

-- tentativa_login is still useful; keep it (it's not "tb_*" but can remain).
-- If you want, we can later rename it to tb_tentativas_login, etc.
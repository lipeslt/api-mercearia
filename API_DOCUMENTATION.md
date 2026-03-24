# API Mercearia - Documentação

## 🎯 Visão Geral

API REST para gerenciamento de e-commerce de mercearia com suporte a produtos, categorias, pedidos e pagamentos.

### Versão: 1.0.0
### Base URL: `http://localhost:8080/api`

---

## 🔐 Autenticação

Todos os endpoints (exceto `/auth/**`) requerem Bearer Token no header:

```bash
Authorization: Bearer <JWT_TOKEN>
```

### Endpoints de Autenticação

#### Registro
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "nome": "João Silva",
  "senha": "senha@Segura123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "senha": "senha@Segura123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

---

## 📦 Endpoints

### Categorias

#### Listar Categorias (Apenas Ativas)
```http
GET /categorias
Authorization: Bearer <TOKEN>

Response: 200 OK
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nome": "Bebidas",
    "descricao": "Refrigerantes, sucos e águas",
    "ativo": true,
    "criadoEm": "2026-03-24T10:22:28",
    "atualizadoEm": "2026-03-24T10:22:28"
  }
]
```

#### Obter Categoria por ID
```http
GET /categorias/{id}
Authorization: Bearer <TOKEN>

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "Bebidas",
  "descricao": "Refrigerantes, sucos e águas",
  "ativo": true,
  "criadoEm": "2026-03-24T10:22:28",
  "atualizadoEm": "2026-03-24T10:22:28"
}
```

#### Criar Categoria
```http
POST /categorias
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "nome": "Bebidas",
  "descricao": "Refrigerantes, sucos e águas"
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "Bebidas",
  "descricao": "Refrigerantes, sucos e águas",
  "ativo": true,
  "criadoEm": "2026-03-24T10:22:28",
  "atualizadoEm": "2026-03-24T10:22:28"
}
```

#### Atualizar Categoria
```http
PUT /categorias/{id}
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "nome": "Bebidas Premium",
  "descricao": "Bebidas gourmet de alta qualidade"
}

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "Bebidas Premium",
  "descricao": "Bebidas gourmet de alta qualidade",
  "ativo": true,
  "criadoEm": "2026-03-24T10:22:28",
  "atualizadoEm": "2026-03-24T11:00:00"
}
```

#### Soft Delete (Desativar Categoria)
```http
DELETE /categorias/{id}
Authorization: Bearer <TOKEN>

Response: 204 No Content
(Categoria não é deletada, apenas marcada como inativa)
```

---

### Produtos

#### Listar Produtos (Apenas Ativos)
```http
GET /produtos
Authorization: Bearer <TOKEN>

Response: 200 OK
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "nome": "Suco Natural Laranja",
    "descricao": "Suco natural fresco",
    "preco": 7.99,
    "estoque": 50,
    "categoria": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "nome": "Bebidas"
    },
    "ativo": true,
    "criadoEm": "2026-03-24T10:22:28",
    "atualizadoEm": "2026-03-24T10:22:28"
  }
]
```

#### Listar Produtos por Categoria
```http
GET /produtos?categoria={categoriaId}
Authorization: Bearer <TOKEN>

Response: 200 OK
[...]
```

#### Buscar Produtos por Nome
```http
GET /produtos/buscar?nome=suco
Authorization: Bearer <TOKEN>

Response: 200 OK
[...]
```

#### Criar Produto
```http
POST /produtos
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "nome": "Suco Natural Laranja",
  "descricao": "Suco natural fresco",
  "preco": 7.99,
  "estoque": 50,
  "categoriaId": "550e8400-e29b-41d4-a716-446655440000"
}

Response: 201 Created
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "nome": "Suco Natural Laranja",
  "preco": 7.99,
  "estoque": 50,
  "ativo": true,
  "criadoEm": "2026-03-24T10:22:28",
  "atualizadoEm": "2026-03-24T10:22:28"
}
```

---

## 🔄 Soft Delete Pattern

O sistema implementa **Soft Delete** para todos os recursos. Recursos deletados não são removidos do banco, apenas marcados como inativos.

### Comportamento:
- **GET** retorna apenas registros `ativo = true`
- **DELETE** marca o registro como `ativo = false`
- **Admin** pode recuperar registros via endpoint especial

### Vantagens:
✅ Auditoria completa  
✅ Recuperação de dados acidentalmente deletados  
✅ Histórico completo mantido  
✅ Referências FK intactas

---

## 📊 Status Codes

| Código | Significado |
|--------|------------|
| 200 | OK - Requisição bem-sucedida |
| 201 | Created - Recurso criado |
| 204 | No Content - Soft delete bem-sucedido |
| 400 | Bad Request - Dados inválidos |
| 401 | Unauthorized - Token inválido/ausente |
| 403 | Forbidden - Sem permissão |
| 404 | Not Found - Recurso não encontrado |
| 500 | Internal Server Error |

---

## 🗄️ Modelo de Dados

### Tabelas Principais

```
tb_usuarios (UUID)
├── id (UUID) [PK]
├── email
├── senha (hashed)
├── nome
├── role (USER, ADMIN)
├── ativo
└── timestamps

tb_categorias (UUID)
├── id (UUID) [PK]
├── nome
├── descricao
├── ativo
└── timestamps

tb_produtos (UUID)
├── id (UUID) [PK]
├── nome
├── descricao
├── preco
├── estoque
├── categoria_id [FK]
├── ativo
└── timestamps

tb_pedidos (UUID)
├── id (UUID) [PK]
├── usuario_id [FK]
├── status (ENUM)
├── valor_total
├── data_criacao
├── ativo
└── timestamps

tb_itens_pedido (UUID)
├── id (UUID) [PK]
├── pedido_id [FK]
├── produto_id [FK]
├── quantidade
├── preco_unitario
├── ativo
└── timestamps

payments (BIGINT)
├── id [PK]
├── pedido_id [FK]
├── mercado_pago_id
├── status (ENUM)
├── amount
└── timestamps
```

---

## 🔒 Segurança

- JWT com expiração configurável
- Senha hasheada com BCrypt
- Rate Limiting por IP
- CORS configurado
- SQL Injection protection (JPA parameterized queries)

---

## 📝 Rate Limiting

- 100 requisições por 1 minuto por IP
- Header: `X-RateLimit-Remaining`

---

## 🚀 Exemplos com cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "senha": "senha@Segura123"
  }'
```

### Listar Categorias
```bash
curl -X GET http://localhost:8080/api/categorias \
  -H "Authorization: Bearer <TOKEN>"
```

### Criar Produto
```bash
curl -X POST http://localhost:8080/api/produtos \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Suco Natural",
    "preco": 7.99,
    "estoque": 50,
    "categoriaId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

**Última atualização**: 24/03/2026  
**Versão**: 1.0.0
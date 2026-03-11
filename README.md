# 🛒 API E-commerce Mercearia (Zaflas)

Um backend robusto, seguro e escalável para e-commerce, desenvolvido com **Java e Spring Boot**. Este projeto implementa boas práticas de mercado, incluindo segurança avançada com JWT, uso de UUIDs para chaves primárias e transações seguras de banco de dados.

## 🚀 Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 3.x** (Web, Data JPA, Security, Validation)
* **PostgreSQL** (Banco de dados relacional)
* **JSON Web Tokens (JWT)** via Auth0 (Autenticação Stateless)
* **Lombok** (Redução de boilerplate)
* **Maven** (Gerenciador de dependências)

## ⚙️ Arquitetura e Boas Práticas

* **Padrão em Camadas:** Separação clara entre `Controllers`, `Services`, `Repositories` e `Models`.
* **Data Transfer Objects (DTOs):** Uso de Java Records para blindar as entidades do banco de dados e validar entradas via *Jakarta Validation*.
* **Tratamento de Erros Global:** Implementação de `@RestControllerAdvice` para padronizar as respostas de erro da API (RFC 7807), ocultando *stack traces* do usuário final.
* **Segurança (Spring Security):** Rotas protegidas por perfis (`ADMIN`, `CLIENTE`), senhas com hash forte (`BCrypt`) e autenticação via *Bearer Token*.
* **Regras de Negócio Transacionais:** Uso de `@Transactional` para garantir a integridade do carrinho de compras e o desconto correto do estoque (Rollback automático em caso de falha).

## 📌 Principais Endpoints

### 🔐 Autenticação
* `POST /api/auth/registrar` - Criação de novo usuário (Hash de senha automático).
* `POST /api/auth/login` - Retorna o Token JWT.

### 📦 Catálogo
* `GET /api/categorias` - Lista categorias (Público).
* `POST /api/categorias` - Cria categoria (Requer Token ADMIN).
* `GET /api/produtos` - Lista produtos (Público).
* `POST /api/produtos` - Cria produto (Requer Token ADMIN).

### 🛒 Vendas
* `POST /api/pedidos` - Finaliza compra, valida e subtrai estoque (Requer Token).

### 📊 Dashboard e CMS
* `GET /api/dashboard/resumo` - Retorna faturamento e total de vendas via JPQL (Requer Token ADMIN).
* `GET /api/carrossel` - Lista banners da página inicial ordenados (Público).
* `POST /api/carrossel` - Adiciona novo banner promocional (Requer Token ADMIN).

## 🛠️ Como executar o projeto localmente

1. Clone o repositório:
   ```bash
   git clone [https://github.com/lipeslt/api-mercearia.git](https://github.com/lipeslt/api-mercearia.git)
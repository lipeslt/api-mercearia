# 🛒 E-commerce Mercearia - API REST

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/lipeslt/api-mercearia)
[![Java Version](https://img.shields.io/badge/java-21-blue)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](#licença)

Uma API REST robusta e segura para gerenciar e-commerce de mercearias. Desenvolvida com **Spring Boot 3.2.3**, **PostgreSQL**, **JWT Authentication** e integração com **Mercado Pago**.

---

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Recursos](#-recursos)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Como Usar](#-como-usar)
- [API Endpoints](#-api-endpoints)
- [Segurança](#-segurança)
- [Testes](#-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Contribuindo](#-contribuindo)
- [Licença](#licença)

---

## 🎯 Visão Geral

Este projeto é uma API REST profissional para gerenciar um e-commerce de mercearia, incluindo:

✅ **Autenticação JWT** com refresh tokens  
✅ **Rate Limiting** para proteção contra ataques  
✅ **Bloqueio de Conta** após tentativas falhas  
✅ **Integração Mercado Pago** para pagamentos  
✅ **Auditoria de Operações** com logs detalhados  
✅ **Cache distribuído** com Redis  
✅ **Validação robusta** de dados  
✅ **Testes automatizados** com cobertura completa

---

## ✨ Recursos

### Autenticação & Segurança
- 🔐 JWT (JSON Web Tokens) com access/refresh tokens
- ⏱️ Access Token (15 minutos) + Refresh Token (7 dias)
- 🔒 Hash de senhas com BCrypt
- 🚫 Rate Limiting (60 req/min por IP)
- 🔐 Bloqueio de conta após 5 tentativas falhas
- 📝 Auditoria completa de operações
- ✅ CORS configurável

### Gerenciamento de Produtos
- 📦 CRUD completo de produtos
- 🏷️ Categorias e filtragem
- 🖼️ Upload de imagens
- 📊 Controle de estoque com pessimistic lock
- ⭐ Avaliações e comentários

### Pedidos & Pagamentos
- 🛒 Carrinho de compras
- 📋 Gerenciamento de pedidos
- 💳 Integração Mercado Pago (Pix, Cartão Crédito)
- 📧 Notificações por email
- 🔔 Webhooks para atualizações de pagamento

### Admin & Relatórios
- 👥 Gerenciamento de usuários
- 📊 Relatórios de vendas (Excel/PDF)
- 🕐 Análise de performance
- 📈 Dashboard de métricas

---

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21+** → [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** → [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 12+** → [Download](https://www.postgresql.org/download/)
- **Git** → [Download](https://git-scm.com/)
- **Docker** (opcional) → [Download](https://www.docker.com/)

### Verificar Instalação

```bash
java -version          # Java 21
mvn -version           # Maven 3.8+
psql --version         # PostgreSQL 12+
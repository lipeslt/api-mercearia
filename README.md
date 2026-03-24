# 🛒 API Mercearia - E-commerce Platform

[![Build Status](https://github.com/lipeslt/api-mercearia/actions/workflows/build.yml/badge.svg)](https://github.com/lipeslt/api-mercearia/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.9-336791)](https://www.postgresql.org/)

Uma API REST robusta, segura e escalável para gerenciamento de e-commerce de mercearia, desenvolvida com Spring Boot 3.2.3 e as melhores práticas de arquitetura moderna.

## 📋 Índice

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Docker Setup](#docker-setup)
- [Database](#database)
- [Security](#security)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## ✨ Features

### 🔐 Autenticação & Autorização
- ✅ JWT (JSON Web Tokens) com refresh token
- ✅ BCrypt password hashing
- ✅ Rate limiting (5 tentativas de login)
- ✅ Bloqueio de conta após falhas repetidas
- ✅ CORS configurado para múltiplas origens

### 📦 Gerenciamento de Dados
- ✅ Soft delete (exclusão lógica)
- ✅ Audit columns (created_at, updated_at, created_by, updated_by)
- ✅ UUID como primary key (segurança)
- ✅ Paginação em todos os endpoints
- ✅ Filtros avançados

### 🛡️ Segurança
- ✅ Spring Security 6.2.2
- ✅ HTTPS Ready
- ✅ Security Headers (CSP, X-Frame-Options)
- ✅ SQL Injection Prevention
- ✅ XSS Protection
- ✅ CSRF Protection

### 🔄 CI/CD & DevOps
- ✅ GitHub Actions (build, test, deploy)
- ✅ Docker & Docker Compose
- ✅ Database migrations (Flyway)
- ✅ Health check endpoint
- ✅ Metrics exposure (Actuator)

### 📊 Observabilidade
- ✅ Request/Response logging
- ✅ Performance metrics
- ✅ Error tracking
- ✅ Distributed tracing ready

### 📚 Documentação
- ✅ Swagger/OpenAPI 3.0
- ✅ Auto-generated API docs
- ✅ Postman collection
- ✅ Architecture documentation

---

## 🛠️ Tech Stack

### Backend
- **Java 21** - Latest LTS version
- **Spring Boot 3.2.3** - Framework web
- **Spring Security 6.2.2** - Autenticação e autorização
- **Spring Data JPA** - ORM
- **Hibernate 6.4.4** - Mapeamento ORM

### Database
- **PostgreSQL 17.9** - Banco de dados relacional
- **Flyway 9.22.3** - Versionamento de schema
- **HikariCP** - Connection pooling

### Development Tools
- **Maven 3.9+** - Build tool
- **Docker** - Containerização
- **Git** - Version control
- **Postman** - API testing

### Libraries
- **Lombok** - Redução de boilerplate
- **Jackson** - JSON processing
- **JWT (Auth0)** - Token management
- **Springdoc OpenAPI** - Swagger/OpenAPI

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- PostgreSQL 15+
- Maven 3.9+
- Docker & Docker Compose (opcional)

### Installation

```bash
# 1. Clone o repositório
git clone https://github.com/lipeslt/api-mercearia.git
cd api-mercearia

# 2. Configure as variáveis de ambiente
cp .env.example .env
nano .env  # Edite com suas credenciais

# 3. Crie o banco de dados
createdb -U postgres mercearia_db

# 4. Execute a aplicação
./mvnw clean spring-boot:run
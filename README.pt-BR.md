# CodeFortress

[English](README.md) | [Português](README.pt-BR.md)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containers-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)


CodeFortress é uma plataforma full-stack de análise estática de segurança criada para identificar riscos de segurança em código-fonte e arquivos de configuração.

A aplicação permite organizar projetos, enviar arquivos de código-fonte, executar análises de segurança de forma assíncrona, investigar findings detectados, acompanhar o Security Score e gerenciar o processo de remediação através de uma interface web.

A versão estável atual é a **v2.0.0**.

---

## Visão geral

O CodeFortress foi desenvolvido como uma plataforma prática de Application Security focada em análise estática determinística.

O fluxo principal é:

```text
Projeto
   |
   v
Upload do código-fonte
   |
   v
Análise assíncrona
   |
   v
Security Engine
   |
   v
Findings
   |
   v
Security Score
   |
   v
Triagem e remediação
```

Cada análise concluída gera findings contendo informações como severidade, arquivo afetado, evidência de código, impacto, recomendação e status de remediação.

---

## Interface

A interface V2 do CodeFortress foi redesenhada como um workspace focado em segurança, com layouts responsivos para desktop e dispositivos móveis.

### Security Dashboard

O Dashboard consolida a postura atual de segurança, distribuição por severidade, histórico do Security Score e atividades recentes de análise.

![Dashboard de segurança do CodeFortress](docs/images/dashboard.png)

### Projetos

Os projetos funcionam como workspaces isolados para organizar análises de código-fonte e acompanhar seu histórico de segurança.

![Projetos do CodeFortress](docs/images/projects.png)

### Análises

Cada projeto mantém um histórico de análises contendo Security Score, quantidade de findings, arquivos analisados e linhas analisadas.

![Análises do CodeFortress](docs/images/analysis.png)

### Findings

Os findings apresentam a regra detectada, severidade, localização no código-fonte, evidência, impacto, recomendação de correção e status do workflow.

![Findings do CodeFortress](docs/images/findings.png)

### Security Rules

O catálogo de Security Rules documenta as verificações determinísticas atualmente habilitadas no Security Engine do CodeFortress.

![Security Rules do CodeFortress](docs/images/security-rules.png)

---

## Principais funcionalidades

### Autenticação

- Cadastro de usuários
- Login com e-mail e senha
- Access Token JWT
- Sessão através de Refresh Token
- Rotação de Refresh Token
- Rotas protegidas
- Logout
- Recuperação automática da sessão

### Gerenciamento de projetos

- Criação de projetos
- Edição de nome e descrição
- Listagem de projetos ativos
- Visualização dos detalhes do projeto
- Arquivamento de projetos
- Isolamento dos recursos por usuário

### Análise de código-fonte

- Upload de arquivos ZIP
- Validação do arquivo enviado
- Extração segura do arquivo
- Descoberta de arquivos analisáveis
- Processamento assíncrono
- Controle do ciclo de vida da análise
- Métricas de arquivos analisados
- Métricas de linhas analisadas
- Histórico de análises por projeto

Estados disponíveis:

```text
QUEUED
RUNNING
COMPLETED
FAILED
CANCELLED
```

### Findings

Cada finding pode apresentar:

- Identificador e versão da regra
- Título
- Categoria
- Severidade
- Status do workflow
- Arquivo afetado
- Linha inicial e final
- Evidência de código
- Descrição
- Impacto de segurança
- Recomendação
- Fingerprint
- Datas de criação e atualização

Status disponíveis:

```text
OPEN
RESOLVED
ACCEPTED_RISK
FALSE_POSITIVE
```

Findings resolvidos ou classificados também podem ser reabertos.

### Filtros de findings

Os findings podem ser filtrados por:

- Status
- Severidade
- Categoria

### Dashboard

O dashboard apresenta uma visão consolidada do workspace com:

- Security Score
- Projetos ativos
- Regras do Security Engine
- Findings abertos
- Findings de alto risco
- Distribuição por severidade
- Histórico recente do Security Score
- Análises recentes
- Última análise concluída

---

## Security Engine

O CodeFortress v2.0.0 possui oito regras determinísticas de análise estática.

| Regra | Detecção | Severidade |
|---|---|---|
| `CF-SEC-001` | Hardcoded Secret | Critical |
| `CF-SEC-002` | Debug Mode Enabled | Medium |
| `CF-SEC-003` | TLS Certificate Verification Disabled | High |
| `CF-SEC-004` | Permissive CORS Configuration | Medium |
| `CF-SEC-005` | SQL Injection Risk | High |
| `CF-SEC-006` | Weak Cryptographic Hash Algorithm | Medium |
| `CF-SEC-007` | Sensitive Information Exposure | High |
| `CF-SEC-008` | Exposed Management Endpoints | High |

As regras identificam **potenciais riscos de segurança através de análise estática**.

Um finding detectado não significa automaticamente que a aplicação analisada seja explorável. O resultado deve ser avaliado considerando o contexto da aplicação.

---

## Security Score

Cada análise concluída recebe um Security Score entre `0` e `100`.

A pontuação começa em:

```text
100
```

e sofre penalizações de acordo com os findings detectados.

Pesos atuais:

| Severidade | Penalização |
|---|---:|
| Critical | 18 |
| High | 8 |
| Medium | 3 |
| Low | 1 |

O score representa a postura de segurança de uma análise específica e é preservado como histórico.

Alterações posteriores no status de um finding não recalculam o score histórico da análise.

---

## Arquitetura

O CodeFortress utiliza uma arquitetura full-stack composta por backend Spring Boot, frontend React e banco de dados PostgreSQL.

### Backend

O backend segue uma abordagem de monólito modular.

Principais áreas da aplicação:

```text
com.codefortress
├── identity
├── project
├── analysis
├── dashboard
└── shared
```

O módulo de análise contém o pipeline de processamento do código-fonte, execução das regras, persistência dos findings, cálculo de score e gerenciamento do ciclo de vida das análises.

### Frontend

O frontend é organizado por funcionalidades.

```text
src
├── app
├── features
├── lib
└── styles
```

O estado proveniente do servidor é gerenciado com TanStack Query. Formulários utilizam React Hook Form e validação com Zod.

---

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Security
- Spring Data JPA
- OAuth2 Resource Server
- Bean Validation
- Spring Boot Actuator
- PostgreSQL
- Flyway
- H2 para testes automatizados
- Maven

### Frontend

- React 19
- TypeScript 6
- Vite 8
- React Router 8
- TanStack Query 5
- React Hook Form 7
- Zod 4
- Oxlint

### Infraestrutura

- Docker
- Docker Compose
- PostgreSQL 17 Alpine

---

## Banco de dados

A evolução do schema é controlada pelo Flyway.

Migrations atuais:

```text
V1 - users
V2 - refresh_tokens
V3 - projects
V4 - analyses
V5 - findings
```

PostgreSQL é utilizado no ambiente de desenvolvimento.

Durante os testes automatizados é utilizado H2 em memória.

---

## Estrutura do repositório

```text
codefortress/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
│
├── docs/
├── compose.yml
├── .env.example
├── README.md
└── README.pt-BR.md
```

---

## Executando localmente

### Requisitos

Antes de executar o projeto, instale:

- Java 21
- Node.js
- npm
- Docker
- Docker Compose

### Clone o repositório

```bash
git clone https://github.com/kayquemigueldev/codefortress.git
cd codefortress
```

### Configure as variáveis de ambiente

Crie o arquivo local:

```bash
cp .env.example .env
```

Exemplo:

```env
POSTGRES_DB=codefortress
POSTGRES_USER=codefortress
POSTGRES_PASSWORD=change_this_local_password
POSTGRES_PORT=5432

JWT_SECRET=replace_with_at_least_32_random_characters
JWT_ISSUER=codefortress-api
JWT_ACCESS_TOKEN_TTL=15m
JWT_REFRESH_TOKEN_TTL=P30D
REFRESH_COOKIE_SECURE=false
```

O arquivo `.env` não deve ser versionado.

### Inicie o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d
```

### Inicie o backend

No macOS ou Linux:

```bash
set -a
source .env
set +a

cd backend
./mvnw spring-boot:run
```

A API será executada localmente em:

```text
http://localhost:8080
```

### Inicie o frontend

Abra outro terminal:

```bash
cd frontend
npm install
npm run dev
```

O servidor de desenvolvimento do Vite encaminha as requisições `/api` para o backend local.

---

## Testes automatizados

Para executar todos os testes do backend:

```bash
cd backend
./mvnw clean test
```

Resultado validado na versão `v2.0.0`:

```text
Tests run: 297
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

A suíte automatizada cobre áreas como:

- Autenticação
- Refresh Tokens
- Usuários
- Projetos
- Arquivamento de projetos
- Upload de arquivos
- Extração segura de arquivos
- Descoberta de arquivos
- Ciclo de vida das análises
- Execução das análises
- Security Rules
- Criação de findings
- Fingerprints
- Persistência de findings
- Workflow de status
- Security Score
- Dashboard
- Controllers da API

---

## Validação do frontend

Build de produção:

```bash
cd frontend
npm run build
```

Lint:

```bash
npm run lint
```

O build de produção da versão `v2.0.0` foi validado com sucesso antes da release.

---

## Responsividade

A interface V2 possui layouts para desktop e dispositivos móveis.

O QA manual responsivo foi realizado utilizando uma viewport de:

```text
390 x 844
```

Foram verificadas as telas:

- Dashboard
- Projects
- Project Overview
- Analyses
- Findings
- Criação de projeto
- Edição de projeto
- Security Rules
- Navegação mobile
- Páginas com conteúdo longo
- Ações de formulários

---

## Validação da versão 2.0

O CodeFortress `v2.0.0` foi validado manualmente com os seguintes fluxos:

- Criação de projeto
- Edição de projeto
- Arquivamento de projeto
- Upload de código-fonte
- Execução de análise
- Detecção de findings
- Visualização das evidências
- Atualização de status
- Reabertura de findings
- Filtro por status
- Filtro por severidade
- Filtro por categoria
- Atualização do Dashboard
- Logout
- Novo login
- Navegação responsiva
- Build de produção do frontend
- Suíte completa do backend

---

## Releases

### v2.0.0

A versão 2 introduziu um redesign completo da interface e da experiência de uso, mantendo o Security Engine e o comportamento existente do backend.

Principais melhorias:

- Novo Dashboard
- Nova interface de Projects
- Novo Project Overview
- Nova experiência de Analyses
- Nova interface de Findings
- Novos formulários de projeto
- Novo catálogo de Security Rules
- Experiência mobile responsiva
- Melhor visualização de severidades
- Melhor apresentação do Security Score
- QA manual completo

### v1.0.0

Primeira versão estável do CodeFortress, contendo o fluxo principal de:

- Autenticação
- Projetos
- Análise de código
- Security Engine
- Findings
- Security Score
- Dashboard

---

## Roadmap

Versões futuras podem explorar funcionalidades como:

- Integração direta com repositórios GitHub
- Análise de repositórios remotos
- Comparação entre análises
- Exportação de relatórios de segurança
- Novas regras de análise estática
- Configuração do Security Engine
- Melhorias de observabilidade

---

## Autor

Desenvolvido por **Kayque Miguel**.

GitHub: [kayquemigueldev](https://github.com/kayquemigueldev)

---

CodeFortress
Analyze code. Find risks. Strengthen security.

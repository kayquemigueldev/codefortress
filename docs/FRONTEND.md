# CodeFortress — Arquitetura do Frontend

## Objetivo

O frontend será uma aplicação React com TypeScript que consumirá dados reais da API.

Ele não será responsável por:

- calcular o Security Score;
- executar regras de segurança;
- decidir se o usuário pode acessar um projeto;
- inventar métricas;
- manter dados falsos como funcionalidade principal.

Essas responsabilidades pertencem ao backend.

## Rotas públicas

```text
/                   Landing page
/login              Login
/register           Cadastro
```

## Rotas protegidas

```text
/app/dashboard
/app/projects
/app/projects/new

/app/projects/:projectId/overview
/app/projects/:projectId/findings
/app/projects/:projectId/history
/app/projects/:projectId/dependencies
/app/projects/:projectId/reports

/app/projects/:projectId/analyses/:analysisId
/app/projects/:projectId/analyses/:analysisId/findings/:findingId
```

Usuários não autenticados serão redirecionados para `/login`.

## Estrutura planejada

```text
src/
├── app/
│   ├── App.tsx
│   ├── router.tsx
│   └── providers.tsx
├── features/
│   ├── auth/
│   ├── dashboard/
│   ├── projects/
│   ├── analyses/
│   ├── findings/
│   ├── dependencies/
│   └── reports/
├── components/
│   ├── layout/
│   ├── feedback/
│   └── ui/
├── lib/
│   ├── api/
│   ├── auth/
│   ├── formatting/
│   └── validation/
├── styles/
├── types/
└── main.tsx
```

A organização será feita por funcionalidade. Não criaremos uma pasta global enorme contendo todos os controllers, hooks ou tipos.

## Estado da aplicação

### Estado vindo do servidor

TanStack Query será utilizado para:

- projetos;
- análises;
- findings;
- dashboard;
- dependências;
- relatórios.

Esses dados não serão duplicados em Context ou Redux.

### Formulários

React Hook Form e Zod serão utilizados para:

- cadastro;
- login;
- criação de projeto;
- edição de projeto;
- upload;
- filtros que exigirem validação.

### Autenticação

Um contexto pequeno armazenará:

- usuário autenticado;
- access token em memória;
- estado de inicialização;
- funções de login e logout.

Não utilizaremos Redux no início. Ele só será introduzido se surgir uma necessidade concreta.

## Inicialização da sessão

Quando a aplicação abrir:

```text
React inicia
    ↓
AuthProvider entra em estado loading
    ↓
POST /api/v1/auth/refresh
    ↓
Recebe access token
    ↓
Carrega usuário
    ↓
Libera as rotas protegidas
```

Se não existir sessão válida, o usuário será tratado como não autenticado.

Durante essa verificação, a aplicação mostrará uma tela de carregamento. Isso evita exibir rapidamente uma página protegida antes do redirecionamento.

## Cliente HTTP

O cliente da API:

1. adicionará `Authorization: Bearer` quando existir token;
2. utilizará `credentials: include` nos endpoints que dependem do cookie;
3. tentará renovar a sessão após um `401`;
4. repetirá a requisição original apenas uma vez;
5. encerrará a sessão se a renovação falhar.

Múltiplos erros `401` simultâneos deverão compartilhar a mesma tentativa de refresh.

Isso evita criar vários refresh tokens ao mesmo tempo.

## Progresso da análise

O stream SSE será consumido com uma solução baseada em `fetch`, permitindo enviar:

```http
Authorization: Bearer access-token
```

Estados visuais:

```text
QUEUED
RUNNING
COMPLETED
FAILED
```

A timeline exibirá apenas eventos recebidos ou consultados do backend.

Se a conexão cair:

1. o frontend tentará reconectar;
2. enviará o último ID recebido;
3. consultará o estado atual da análise;
4. encerrará o stream quando a análise terminar.

## Layout autenticado

```text
┌──────────────────────────────────────────────┐
│ Topbar: projeto atual, busca, usuário         │
├──────────────┬───────────────────────────────┤
│ Sidebar      │ Conteúdo da página            │
│              │                               │
│ Dashboard    │                               │
│ Projects     │                               │
│ Activity     │                               │
│              │                               │
│ Settings     │                               │
└──────────────┴───────────────────────────────┘
```

Em telas menores, a sidebar será recolhida em um menu acessível.

## Dashboard

O dashboard mostrará somente dados reais:

- Security Score mais recente;
- quantidade de projetos;
- análises realizadas;
- findings abertos;
- findings resolvidos;
- distribuição por severidade;
- evolução do score;
- últimas análises;
- projetos com maior risco.

Se ainda não houver análises, será exibido um estado vazio orientando o usuário a criar seu primeiro projeto.

## Página do projeto

Navegação interna:

```text
Overview
Findings
Analysis History
Dependencies
Reports
```

### Overview

- score mais recente;
- diferença para a análise anterior;
- resumo de severidades;
- última análise;
- botão para executar análise;
- atividade recente.

### Findings

Tabela:

```text
Severity | Finding | File | Line | Status
```

Filtros:

- severidade;
- categoria;
- status;
- pesquisa;
- análise selecionada.

Os filtros importantes serão refletidos na URL.

### Finding detail

- título;
- severidade;
- status;
- regra;
- arquivo;
- linhas;
- código mascarado;
- descrição;
- impacto;
- recomendação;
- alteração de status.

### Analysis History

- gráfico de evolução;
- lista de análises;
- duração;
- arquivos analisados;
- quantidade de findings;
- comparação entre execuções.

### Dependencies

- ecossistema;
- nome;
- versão declarada;
- arquivo de origem;
- estado da verificação.

### Reports

- solicitar relatório;
- acompanhar geração;
- baixar PDF pronto;
- visualizar falha de geração.

## Estados obrigatórios

Toda tela que busca dados deverá considerar:

```text
Loading
Success
Empty
Error
Unauthorized
```

Uma tabela vazia não será usada como substituta de um estado vazio explicativo.

## Identidade visual

Direção visual:

- fundo grafite ou azul muito escuro;
- superfícies com contraste discreto;
- texto principal claro;
- texto secundário cinza;
- verde para seguro;
- amarelo para atenção;
- laranja para risco alto;
- vermelho para crítico;
- fonte monoespaçada apenas para código e dados técnicos.

Não utilizaremos verde neon em toda a interface nem elementos decorativos de “hacker simulator”.

## Acessibilidade

- contraste mínimo WCAG AA;
- foco visível;
- navegação por teclado;
- labels associados aos campos;
- mensagens de erro ligadas aos inputs;
- severidade indicada por texto e ícone, não apenas por cor;
- suporte a `prefers-reduced-motion`;
- botões com estados disabled e loading;
- tabelas com cabeçalhos semânticos.

## Componentes compartilhados planejados

```text
AppShell
Sidebar
Topbar
PageHeader
Button
Input
Select
Modal
Drawer
Badge
SeverityBadge
StatusBadge
EmptyState
ErrorState
LoadingState
DataTable
ScoreCard
ScoreChart
AnalysisTimeline
CodeExcerpt
```

Um componente compartilhado somente será criado quando houver reutilização real ou um padrão visual importante.

## Ordem de implementação

```text
1. Base React e tema
2. Router
3. Cliente HTTP
4. AuthProvider
5. Cadastro e login
6. Layout protegido
7. Projetos
8. Upload e progresso
9. Findings
10. Dashboard
11. Histórico
12. Relatórios
```

Cada etapa deverá funcionar com o backend correspondente antes de avançarmos.
# CodeFortress — API do MVP

## Convenções

Base URL:

```text
/api/v1
```

Formato principal:

```text
application/json
```

Datas:

```text
ISO 8601 em UTC
2026-09-02T23:45:00Z
```

Identificadores:

```text
UUID
```

Uploads:

```text
multipart/form-data
```

Progresso:

```text
text/event-stream
```

## Autenticação

Endpoints protegidos recebem:

```http
Authorization: Bearer access-token
```

O UUID do usuário será extraído do JWT validado. A API não aceitará `ownerId` enviado pelo frontend.

## Erros

Erros seguirão o formato Problem Details:

```json
{
  "type": "https://codefortress.dev/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/projects",
  "code": "VALIDATION_ERROR",
  "traceId": "79f9b065a07c",
  "violations": [
    {
      "field": "name",
      "message": "must not be blank"
    }
  ]
}
```

A API nunca devolverá stack trace, SQL ou detalhes internos.

## Paginação

Parâmetros:

```text
page=0
size=20
sort=createdAt,desc
```

Resposta:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

O tamanho máximo de página será limitado pelo backend.

# Auth

## Cadastrar usuário

```http
POST /api/v1/auth/register
```

```json
{
  "displayName": "Kayque Miguel",
  "email": "kayque@example.com",
  "password": "uma senha longa e segura"
}
```

Resposta:

```text
201 Created
```

## Login

```http
POST /api/v1/auth/login
```

```json
{
  "email": "kayque@example.com",
  "password": "uma senha longa e segura"
}
```

Resposta:

```text
200 OK
```

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "displayName": "Kayque Miguel",
    "email": "kayque@example.com"
  }
}
```

O refresh token será enviado por cookie `HttpOnly`.

## Renovar sessão

```http
POST /api/v1/auth/refresh
```

Resposta:

```text
200 OK
```

```json
{
  "accessToken": "novo-jwt",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

## Logout

```http
POST /api/v1/auth/logout
```

Resposta:

```text
204 No Content
```

## Usuário atual

```http
GET /api/v1/auth/me
```

Resposta:

```json
{
  "id": "uuid",
  "displayName": "Kayque Miguel",
  "email": "kayque@example.com"
}
```

# Projects

## Criar projeto

```http
POST /api/v1/projects
```

```json
{
  "name": "Customer API",
  "description": "Spring Boot API used by the customer portal"
}
```

Resposta:

```text
201 Created
Location: /api/v1/projects/{projectId}
```

```json
{
  "id": "uuid",
  "name": "Customer API",
  "description": "Spring Boot API used by the customer portal",
  "status": "ACTIVE",
  "latestScore": null,
  "createdAt": "2026-09-02T23:45:00Z",
  "updatedAt": "2026-09-02T23:45:00Z"
}
```

## Listar projetos

```http
GET /api/v1/projects?page=0&size=20&sort=createdAt,desc
```

Filtros opcionais:

```text
status=ACTIVE
query=customer
```

## Visualizar projeto

```http
GET /api/v1/projects/{projectId}
```

## Editar projeto

```http
PATCH /api/v1/projects/{projectId}
```

```json
{
  "name": "Customer Platform API",
  "description": "Updated description"
}
```

## Arquivar projeto

```http
DELETE /api/v1/projects/{projectId}
```

Resposta:

```text
204 No Content
```

A operação será uma exclusão lógica.

# Analyses

## Iniciar análise

```http
POST /api/v1/projects/{projectId}/analyses
Content-Type: multipart/form-data
Idempotency-Key: uuid-opcional
```

Campos:

```text
file: project.zip
authorizationConfirmed: true
```

A requisição será rejeitada se `authorizationConfirmed` não for verdadeiro.

Resposta:

```text
202 Accepted
Location: /api/v1/projects/{projectId}/analyses/{analysisId}
```

```json
{
  "id": "uuid",
  "projectId": "uuid",
  "sequenceNumber": 1,
  "status": "QUEUED",
  "createdAt": "2026-09-02T23:45:00Z"
}
```

## Listar análises

```http
GET /api/v1/projects/{projectId}/analyses?page=0&size=20
```

## Visualizar análise

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}
```

Resposta:

```json
{
  "id": "uuid",
  "projectId": "uuid",
  "sequenceNumber": 3,
  "status": "COMPLETED",
  "securityScore": 89,
  "scoreVersion": "score-v1",
  "ruleSetVersion": "ruleset-java-v1",
  "filesScanned": 184,
  "linesScanned": 12940,
  "findingsCount": 7,
  "severityCounts": {
    "critical": 0,
    "high": 0,
    "medium": 2,
    "low": 5
  },
  "startedAt": "2026-09-02T23:45:02Z",
  "completedAt": "2026-09-02T23:45:09Z",
  "createdAt": "2026-09-02T23:45:00Z"
}
```

## Progresso via SSE

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/events
Accept: text/event-stream
Authorization: Bearer access-token
```

Evento:

```text
id: 148
event: analysis-progress
data: {"stage":"SOURCE_ANALYSIS","progress":40,"message":"Analyzing Java source files","createdAt":"2026-09-02T23:45:04Z"}
```

Evento final:

```text
event: analysis-completed
data: {"analysisId":"uuid","securityScore":89}
```

O cliente enviará `Last-Event-ID` ao reconectar para evitar perder eventos.

### Decisão do frontend para SSE

O `EventSource` nativo do navegador não permite definir o header `Authorization`.

Por isso, o frontend consumirá o stream com `fetch` autenticado ou uma biblioteca baseada em fetch.

O token não será colocado na URL, pois URLs podem aparecer em históricos e logs.

## Comparar análises

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/comparison?baseline={previousAnalysisId}
```

Resposta:

```json
{
  "baselineAnalysisId": "uuid",
  "currentAnalysisId": "uuid",
  "scoreChange": 15,
  "newFindings": 1,
  "persistentFindings": 3,
  "fixedFindings": 5,
  "ruleSetChanged": false,
  "scoreVersionChanged": false
}
```

# Findings

## Listar findings

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/findings
```

Filtros opcionais:

```text
severity=HIGH
status=OPEN
category=CODE
query=sql
page=0
size=20
sort=severity,desc
```

Item resumido:

```json
{
  "id": "uuid",
  "ruleKey": "CF-CODE-001",
  "title": "Potentially unsafe SQL construction",
  "category": "CODE",
  "severity": "HIGH",
  "status": "OPEN",
  "filePath": "src/main/java/UserRepository.java",
  "startLine": 87
}
```

## Detalhar finding

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/findings/{findingId}
```

Resposta:

```json
{
  "id": "uuid",
  "ruleKey": "CF-CODE-001",
  "ruleVersion": "1.0.0",
  "title": "Potentially unsafe SQL construction",
  "category": "CODE",
  "severity": "HIGH",
  "status": "OPEN",
  "filePath": "src/main/java/UserRepository.java",
  "startLine": 87,
  "endLine": 88,
  "codeExcerpt": "String sql = \"SELECT ...\" + userId;",
  "description": "SQL is constructed using concatenated input.",
  "impact": "Untrusted input may change the intended query.",
  "recommendation": "Use a parameterized query.",
  "createdAt": "2026-09-02T23:45:07Z"
}
```

## Alterar status

```http
PATCH /api/v1/projects/{projectId}/analyses/{analysisId}/findings/{findingId}/status
```

```json
{
  "status": "ACCEPTED_RISK"
}
```

Resposta:

```text
200 OK
```

A alteração não recalculará retroativamente o score da análise.

# Dependencies

## Listar dependências

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/dependencies
```

Filtros:

```text
ecosystem=MAVEN
status=VULNERABLE
page=0
size=20
```

# Dashboard

## Resumo

```http
GET /api/v1/dashboard/summary
```

Resposta:

```json
{
  "projectsCount": 4,
  "analysesCount": 18,
  "openFindingsCount": 26,
  "resolvedFindingsCount": 14,
  "severityCounts": {
    "critical": 2,
    "high": 7,
    "medium": 13,
    "low": 4
  },
  "latestAnalyses": [],
  "highestRiskProjects": [],
  "scoreTrend": []
}
```

Todos os valores serão calculados usando somente dados do usuário autenticado.

# Reports

## Solicitar relatório

```http
POST /api/v1/projects/{projectId}/analyses/{analysisId}/reports
```

Resposta:

```text
202 Accepted
```

```json
{
  "id": "uuid",
  "status": "QUEUED",
  "createdAt": "2026-09-02T23:45:00Z"
}
```

## Consultar relatório

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/reports/{reportId}
```

## Baixar relatório

```http
GET /api/v1/projects/{projectId}/analyses/{analysisId}/reports/{reportId}/download
```

O download somente será permitido quando o relatório estiver com status `READY`.

# Status HTTP

| Status | Uso |
|---:|---|
| 200 | Consulta ou atualização concluída |
| 201 | Recurso criado |
| 202 | Processamento assíncrono aceito |
| 204 | Operação concluída sem corpo |
| 400 | Requisição inválida |
| 401 | Usuário não autenticado |
| 403 | Operação conhecida, mas não permitida |
| 404 | Recurso inexistente ou pertencente a outro usuário |
| 409 | Conflito, duplicidade ou estado incompatível |
| 413 | Upload maior que o permitido |
| 415 | Formato de arquivo não suportado |
| 422 | Conteúdo válido, mas impossível de processar |
| 429 | Limite de requisições excedido |
| 500 | Falha interna sem detalhes sensíveis |

# Regras de autorização

Antes de acessar uma análise, finding ou relatório, o backend verificará toda a cadeia:

```text
recurso
    ↓ pertence à análise
análise
    ↓ pertence ao projeto
projeto
    ↓ pertence ao usuário autenticado
usuário
```

Um UUID válido não concede acesso ao recurso.
# CodeFortress — Autenticação e Autorização

## Objetivo

Permitir que usuários se autentiquem com segurança e acessem somente os próprios dados.

## Estratégia

O CodeFortress utilizará:

- BCrypt para hash de senha;
- JWT como access token;
- refresh token aleatório e rotativo;
- access token mantido apenas em memória no frontend;
- refresh token enviado por cookie protegido;
- endpoints protegidos por padrão.

## Fluxo de cadastro

```text
Frontend
    ↓ POST /api/v1/auth/register
Controller
    ↓
Validation
    ↓
AuthService
    ↓
Normalização do e-mail
    ↓
Verificação de duplicidade
    ↓
Hash BCrypt
    ↓
UserRepository
    ↓
PostgreSQL
```

Requisição:

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

```json
{
  "id": "uuid",
  "displayName": "Kayque Miguel",
  "email": "kayque@example.com"
}
```

O cadastro não fará login automaticamente. Isso mantém cadastro e criação de sessão como operações separadas e fáceis de testar.

## Regras da senha

- mínimo de 12 caracteres;
- máximo de 72 bytes por causa do BCrypt;
- espaços serão permitidos;
- a senha não será modificada ou normalizada;
- não exigiremos combinações artificiais de símbolos;
- o hash nunca aparecerá em respostas ou logs.

Senhas longas ou passphrases serão incentivadas.

## Fluxo de login

```text
Frontend
    ↓ POST /api/v1/auth/login
AuthController
    ↓
AuthService
    ↓
Busca usuário por e-mail normalizado
    ↓
BCrypt verifica a senha
    ↓
Access token JWT é criado
    ↓
Refresh token aleatório é criado
    ↓
Hash do refresh token é salvo
    ↓
Token original é enviado em cookie HttpOnly
```

Requisição:

```json
{
  "email": "kayque@example.com",
  "password": "uma senha longa e segura"
}
```

Resposta:

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

O access token terá duração inicial de 15 minutos.

## Access token

O JWT conterá apenas dados necessários:

```json
{
  "sub": "user-uuid",
  "iss": "codefortress-api",
  "iat": 1788300000,
  "exp": 1788300900,
  "jti": "token-uuid"
}
```

- `sub`: identificador do usuário;
- `iss`: aplicação que emitiu o token;
- `iat`: momento da emissão;
- `exp`: expiração;
- `jti`: identificador único do JWT.

O token será assinado com HMAC SHA-256 usando um segredo forte armazenado em variável de ambiente.

O segredo:

- terá no mínimo 256 bits;
- não será colocado no Git;
- não será enviado ao frontend;
- deverá vir de um secret manager em produção.

## Armazenamento no frontend

O access token será mantido apenas na memória da aplicação React.

Não utilizaremos `localStorage` ou `sessionStorage` para armazenar tokens.

Quando a página for recarregada:

1. o frontend perde o access token em memória;
2. chama o endpoint de renovação;
3. o navegador envia o cookie de refresh;
4. o backend devolve um access token novo.

## Refresh token

O refresh token será uma sequência aleatória criptograficamente segura.

Configuração inicial:

```text
Validade: 30 dias
Cookie: cf_refresh
HttpOnly: true
Secure: true em produção
SameSite: Strict
Path: /api/v1/auth
```

O banco armazenará somente:

```text
SHA-256(refreshToken)
```

Se o banco for exposto, o token original não poderá ser utilizado diretamente.

## Rotação do refresh token

Cada renovação invalida o refresh token anterior.

```text
Refresh token A
    ↓ utilizado
Token A é revogado
    ↓
Refresh token B é criado
    ↓
Token A registra replaced_by_id = B
```

Todos os tokens da mesma sessão compartilham o mesmo `family_id`.

## Detecção de reutilização

Se um refresh token já rotacionado for utilizado novamente:

1. o backend identifica a reutilização;
2. todos os tokens daquela `family_id` são revogados;
3. o usuário precisa fazer login novamente.

Isso reduz o impacto do roubo de um refresh token.

## Renovação

Endpoint:

```text
POST /api/v1/auth/refresh
```

Não haverá token no corpo. O navegador enviará o cookie automaticamente.

Resposta:

```json
{
  "accessToken": "novo-jwt",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

A resposta também criará um novo cookie de refresh.

## Logout

Endpoint:

```text
POST /api/v1/auth/logout
```

O backend:

1. procura o hash do refresh token;
2. revoga o token;
3. expira o cookie;
4. devolve `204 No Content`.

Apagar apenas o cookie no navegador não seria suficiente, pois o token continuaria válido no backend.

## Usuário autenticado

Endpoint:

```text
GET /api/v1/auth/me
```

Header:

```http
Authorization: Bearer access-token
```

Resposta:

```json
{
  "id": "uuid",
  "displayName": "Kayque Miguel",
  "email": "kayque@example.com"
}
```

## Proteção contra CSRF

Access tokens são enviados manualmente pelo header `Authorization`, portanto não são enviados automaticamente pelo navegador.

Refresh e logout utilizam cookies. Esses endpoints terão:

- `SameSite=Strict`;
- validação do header `Origin`;
- frontend e backend sob domínios controlados;
- CORS com origem explícita;
- credenciais permitidas apenas para o frontend autorizado.

## CORS

Em desenvolvimento, a origem permitida será:

```text
http://localhost:5173
```

Não utilizaremos:

```text
Access-Control-Allow-Origin: *
```

junto com cookies ou credenciais.

Em produção, a origem virá de variável de ambiente.

## Erros de autenticação

Login com e-mail inexistente e login com senha incorreta produzirão a mesma resposta:

```text
401 Unauthorized
E-mail ou senha inválidos.
```

Isso evita revelar facilmente quais e-mails estão cadastrados.

Respostas nunca deverão conter:

- senha;
- hash da senha;
- refresh token;
- segredo JWT;
- stack trace;
- detalhes internos do banco.

## Autorização

Todos os endpoints serão protegidos, exceto:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /actuator/health
```

Permitir um endpoint na configuração de segurança não significa que ele ficará sem validações próprias.

## Isolamento de recursos

O UUID do usuário virá do JWT validado, nunca de um campo enviado pelo frontend.

Incorreto:

```text
POST /projects
{
  "ownerId": "uuid-enviado-pelo-frontend"
}
```

Correto:

```text
Usuário do JWT
    ↓
ProjectService
    ↓
Novo projeto recebe o ID autenticado
```

O frontend nunca escolherá o proprietário de um projeto.

## Testes obrigatórios

- cadastro salva senha com hash;
- cadastro rejeita e-mail duplicado;
- login correto cria tokens;
- senha incorreta retorna resposta genérica;
- usuário desativado não autentica;
- access token expirado é rejeitado;
- refresh token válido é rotacionado;
- refresh token revogado é rejeitado;
- reutilização revoga toda a família;
- logout revoga o token;
- endpoint protegido rejeita usuário não autenticado;
- dados sensíveis não aparecem nas respostas.
- 
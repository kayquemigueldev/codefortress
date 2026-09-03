# CodeFortress — MVP v1

## Objetivo

Construir uma aplicação web em que um usuário autenticado possa criar um projeto, enviar um código autorizado em formato ZIP, executar uma análise estática real e visualizar os problemas de segurança encontrados.

## Fluxo principal

1. O usuário cria uma conta.
2. O usuário faz login.
3. O usuário cria um projeto.
4. O usuário envia um arquivo ZIP contendo código autorizado.
5. O backend recebe e valida o arquivo.
6. O sistema analisa os arquivos sem executar o código.
7. As regras de segurança geram findings determinísticos.
8. O backend calcula e salva o Security Score.
9. O frontend apresenta o resultado da análise.
10. O usuário consulta os detalhes dos findings.
11. O usuário altera o status de um finding.
12. O usuário realiza uma nova análise e acompanha a evolução do projeto.

## Funcionalidades incluídas

### Autenticação

- Cadastro de usuário.
- Login.
- Senha armazenada com hash seguro.
- Access Token JWT.
- Refresh Token.
- Logout.
- Rotas protegidas.
- Isolamento dos dados por usuário.

### Projetos

- Criar projeto.
- Listar projetos do usuário.
- Visualizar um projeto.
- Editar nome e descrição.
- Arquivar projeto.

### Análises

- Upload de projeto em formato ZIP.
- Confirmação de que o usuário possui autorização para analisar o código.
- Validação segura do arquivo.
- Processamento assíncrono.
- Progresso real da análise.
- Histórico de análises.
- Comparação com a análise anterior.

### Regras iniciais

- Senha, token ou API key escrita diretamente no código.
- Material de chave privada.
- CORS excessivamente permissivo.
- Debug ou Actuator exposto.
- Construção potencialmente insegura de SQL.
- Log contendo informação potencialmente sensível.

### Findings

Cada finding deve mostrar:

- título;
- categoria;
- severidade;
- status;
- arquivo;
- linha;
- trecho relevante mascarado;
- descrição;
- impacto;
- recomendação.

Status disponíveis:

- Open;
- Resolved;
- Accepted Risk;
- False Positive.

### Security Score

- Começa em 100.
- Findings reduzem o score de acordo com a severidade.
- O cálculo acontece no backend.
- O resultado é salvo no banco.
- Cada análise mantém seu próprio score.
- A evolução é apresentada em um gráfico.

### Dashboard

- Quantidade de projetos.
- Quantidade de análises.
- Findings por severidade.
- Findings resolvidos.
- Security Score atual.
- Evolução do score.
- Últimas análises.
- Projetos com maior risco.

### Relatório

- Geração de relatório PDF para uma análise.
- Resumo do projeto.
- Security Sco
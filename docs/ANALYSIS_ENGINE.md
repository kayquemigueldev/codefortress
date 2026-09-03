# CodeFortress — Analysis Engine

## Responsabilidade

O Analysis Engine recebe arquivos de um projeto autorizado, aplica regras estáticas determinísticas e devolve possíveis problemas de segurança.

O motor:

- não executa código;
- não compila o projeto;
- não inicia processos do projeto;
- não faz requisições definidas pelo projeto;
- não inventa findings com inteligência artificial;
- não depende de controllers, banco de dados ou frontend.

## Entrada e saída

Entrada:

```text
Conjunto de arquivos de texto normalizados
+
Contexto da análise
+
Regras habilitadas
```

Saída:

```text
Lista de RuleMatch
+
Métricas da execução
```

A camada de aplicação transforma cada `RuleMatch` em um `Finding` persistido.

## Pipeline

```mermaid
flowchart TD
    A[ZIP recebido] --> B[Validar upload]
    B --> C[Extrair com segurança]
    C --> D[Descobrir arquivos]
    D --> E[Ignorar arquivos desnecessários]
    E --> F[Identificar tipos]
    F --> G[Ler conteúdo como texto]
    G --> H[Selecionar regras compatíveis]
    H --> I[Executar regras]
    I --> J[Mascarar evidências]
    J --> K[Remover duplicidades]
    K --> L[Gerar fingerprints]
    L --> M[Devolver resultados]
```

## Limites iniciais

| Limite | Valor |
|---|---:|
| Tamanho máximo do ZIP | 25 MB |
| Tamanho máximo descompactado | 100 MB |
| Quantidade máxima de arquivos | 5.000 |
| Tamanho máximo por arquivo analisado | 1 MB |
| Profundidade máxima de diretórios | 25 |
| Tempo máximo inicial | 2 minutos |

Esses valores serão configuráveis por propriedades do backend.

## Extração segura

Para cada item do ZIP:

1. normalizar o caminho;
2. resolver o destino dentro do diretório temporário;
3. verificar se o destino continua dentro desse diretório;
4. rejeitar caminhos absolutos;
5. rejeitar caminhos contendo escape por `..`;
6. rejeitar links simbólicos;
7. acompanhar o total de bytes extraídos;
8. interromper ao ultrapassar algum limite.

Isso protege contra ataques como Zip Slip e arquivos compactados maliciosos.

## Diretórios ignorados

```text
.git
.idea
.vscode
node_modules
target
build
dist
coverage
out
vendor
```

## Arquivos analisados inicialmente

```text
.java
.properties
.yml
.yaml
.xml
.json
```

Arquivos binários serão ignorados.

Arquivos `.class`, `.jar`, imagens, vídeos e executáveis não serão processados como código-fonte.

## Contratos planejados

### `ScannableFile`

```java
public record ScannableFile(
    String normalizedPath,
    FileType type,
    String content,
    int lineCount
) {}
```

### `AnalysisContext`

```java
public record AnalysisContext(
    UUID analysisId,
    String ruleSetVersion
) {}
```

### `SecurityRule`

```java
public interface SecurityRule {

    RuleMetadata metadata();

    boolean supports(ScannableFile file);

    List<RuleMatch> evaluate(
        ScannableFile file,
        AnalysisContext context
    );
}
```

### `RuleMetadata`

```java
public record RuleMetadata(
    String key,
    String version,
    String title,
    FindingCategory category,
    Severity defaultSeverity,
    String description,
    String impact,
    String recommendation
) {}
```

### `RuleMatch`

```java
public record RuleMatch(
    String ruleKey,
    Severity severity,
    String filePath,
    int startLine,
    int endLine,
    String redactedEvidence
) {}
```

Esses contratos são planejados. Poderão ser ajustados durante a implementação se os testes demonstrarem uma necessidade concreta.

## Primeiras regras

### `CF-SEC-001` — Hardcoded Secret

Detecta possíveis senhas, tokens, API keys e JWT secrets escritos diretamente no código ou configuração.

Exemplos suspeitos:

```java
String password = "super-secret-password";
String apiKey = "sk_live_example_value";
```

```properties
spring.datasource.password=real-password-value
```

Não deve sinalizar automaticamente:

```java
String password = System.getenv("DB_PASSWORD");
String token = "";
String apiKey = "${API_KEY}";
```

O valor encontrado deverá ser mascarado:

```text
super-****************
```

Severidade padrão: `CRITICAL`.

### `CF-SEC-002` — Private Key Material

Detecta cabeçalhos conhecidos de chaves privadas:

```text
-----BEGIN PRIVATE KEY-----
-----BEGIN RSA PRIVATE KEY-----
-----BEGIN EC PRIVATE KEY-----
```

O conteúdo da chave nunca deverá aparecer no finding ou nos logs.

Severidade padrão: `CRITICAL`.

### `CF-CFG-001` — Insecure CORS

Detecta configurações excessivamente permissivas.

Exemplos:

```java
@CrossOrigin(origins = "*")
```

```java
configuration.setAllowedOrigins(List.of("*"));
```

O risco aumenta quando origens universais são combinadas com credenciais.

Severidade padrão: `HIGH`.

### `CF-CFG-002` — Debug or Actuator Exposure

Detecta configurações como:

```properties
debug=true
management.endpoints.web.exposure.include=*
```

Nem toda exposição do Actuator possui o mesmo impacto. A regra deverá explicar exatamente qual configuração foi encontrada.

Severidade padrão: `MEDIUM` para debug e `HIGH` para exposição ampla do Actuator.

### `CF-CODE-001` — Unsafe SQL Construction

Detecta padrões de SQL construído por concatenação ou interpolação próximo a APIs conhecidas.

Exemplo:

```java
String sql = "SELECT * FROM users WHERE id = " + userId;
jdbcTemplate.query(sql, rowMapper);
```

Não deve sinalizar uma consulta parametrizada:

```java
jdbcTemplate.query(
    "SELECT * FROM users WHERE id = ?",
    rowMapper,
    userId
);
```

O finding será descrito como “construção potencialmente insegura”, pois uma análise textual não prova sozinha que existe exploração.

Severidade padrão: `HIGH`.

### `CF-CODE-002` — Sensitive Logging

Detecta valores potencialmente sensíveis enviados para logs.

Exemplo:

```java
log.info("User password: {}", password);
log.debug("Authorization token: {}", token);
```

Não deve sinalizar apenas porque uma mensagem contém a palavra `password` sem registrar seu valor:

```java
log.info("Password updated successfully");
```

Severidade padrão: `MEDIUM`.

## Regras determinísticas

Uma regra deve produzir o mesmo resultado quando receber:

- o mesmo conteúdo;
- o mesmo caminho;
- a mesma versão da regra;
- a mesma configuração.

Cada regra terá uma versão própria:

```text
CF-SEC-001@1.0.0
```

O conjunto completo também terá uma versão:

```text
ruleset-java-v1
```

## Fingerprint

O fingerprint correlaciona o mesmo problema entre análises.

```text
SHA-256(
    ruleKey
    + normalizedFilePath
    + normalizedEvidence
)
```

Informações secretas serão mascaradas ou transformadas antes da persistência.

## Falsos positivos

Uma regra textual trabalha com evidências, não com certeza absoluta.

Para reduzir falsos positivos:

- ignorar placeholders conhecidos;
- reconhecer variáveis de ambiente;
- analisar contexto próximo;
- diferenciar SQL concatenado de SQL parametrizado;
- usar nomes de variáveis como sinal, não como única prova;
- criar exemplos positivos e negativos para cada regra.

A interface utilizará expressões como:

```text
Potential security issue
Potentially unsafe construction
Review required
```

## Progresso

O motor notificará etapas reais:

```text
5%  — Validating upload
15% — Extracting files
25% — Discovering files
40% — Analyzing source code
60% — Analyzing configuration
75% — Analyzing dependencies
90% — Saving findings
100% — Analysis completed
```

Esses eventos serão persistidos antes de serem enviados ao frontend via SSE.

## Tratamento de falhas

Uma falha deverá:

1. interromper o processamento;
2. marcar a análise como `FAILED`;
3. registrar um código de erro seguro;
4. remover arquivos temporários;
5. não deixar resultados parciais como análise concluída;
6. não expor caminhos internos ou conteúdo do projeto.

Exemplos de códigos:

```text
INVALID_ARCHIVE
ARCHIVE_TOO_LARGE
TOO_MANY_FILES
UNSUPPORTED_ENCODING
ANALYSIS_TIMEOUT
INTERNAL_ANALYSIS_ERROR
```

## Testes obrigatórios

Cada regra terá:

- caso positivo;
- caso negativo;
- mais de uma ocorrência;
- número correto da linha;
- caminho correto;
- severidade correta;
- evidência mascarada;
- ausência de segredo nos resultados.

O pipeline terá testes para:

- ZIP válido;
- arquivo que tenta escapar do diretório;
- limite de tamanho;
- limite de quantidade;
- arquivo binário;
- diretório ignorado;
- remoção do diretório temporário;
- falha durante processamento;
- resultado determinístico.
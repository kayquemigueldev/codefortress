# CodeFortress — Security Score v1

## Objetivo

O Security Score resume os resultados de uma análise em um número entre 0 e 100.

O score:

- será calculado exclusivamente no backend;
- será salvo junto com a análise;
- terá uma fórmula versionada;
- não substituirá CVSS;
- não será apresentado como certificação de segurança;
- permitirá acompanhar a evolução de um projeto.

Versão inicial:

```text
score-v1
```

## Faixas

| Score | Classificação |
|---:|---|
| 90–100 | Strong |
| 75–89 | Moderate |
| 50–74 | High Risk |
| 0–49 | Critical Risk |

As cores da interface serão auxiliares. Texto e ícones também indicarão a classificação.

## Pesos

| Severidade | Peso |
|---|---:|
| Critical | 18 |
| High | 8 |
| Medium | 3 |
| Low | 1 |

Uma contagem linear faria dezenas de findings semelhantes destruírem o score rapidamente.

Por isso, ocorrências repetidas da mesma severidade terão penalização com crescimento reduzido.

## Fórmula de penalização

Para cada severidade:

```text
se count = 0:
    penalty = 0

se count > 0:
    penalty = weight × (1 + 0.5 × ln(count))
```

Em que:

- `weight` é o peso da severidade;
- `count` é a quantidade encontrada;
- `ln` é o logaritmo natural.

Penalização total:

```text
totalPenalty =
    criticalPenalty
    + highPenalty
    + mediumPenalty
    + lowPenalty
```

Score inicial:

```text
rawScore = 100 - totalPenalty
```

Limite final:

```text
score = round(clamp(rawScore, 0, 100))
```

## Limites por severidade

Somente a penalização matemática poderia permitir score alto mesmo com um problema grave.

Aplicaremos limites:

```text
Se existir Critical:
    score máximo = 49

Senão, se existir High:
    score máximo = 74

Senão, se existir Medium:
    score máximo = 89

Somente Low ou nenhum finding:
    não existe limite adicional
```

Exemplo: um projeto com apenas um finding Critical não poderá aparecer como “Strong”.

## Pseudocódigo

```java
int calculateScore(FindingCounts counts) {
    double penalty = 0;

    penalty += calculatePenalty(18, counts.critical());
    penalty += calculatePenalty(8, counts.high());
    penalty += calculatePenalty(3, counts.medium());
    penalty += calculatePenalty(1, counts.low());

    int score = roundAndClamp(100 - penalty);

    if (counts.critical() > 0) {
        return Math.min(score, 49);
    }

    if (counts.high() > 0) {
        return Math.min(score, 74);
    }

    if (counts.medium() > 0) {
        return Math.min(score, 89);
    }

    return score;
}
```

## Exemplos

### Nenhum finding

```text
Critical: 0
High: 0
Medium: 0
Low: 0

Score: 100
```

### Um finding Low

```text
Low penalty = 1 × (1 + 0.5 × ln(1))
Low penalty = 1

Score: 99
```

### Um finding Medium

```text
Raw score: 97
Limite por Medium: 89

Score: 89
```

### Um finding High

```text
Raw score: 92
Limite por High: 74

Score: 74
```

### Um finding Critical

```text
Raw score: 82
Limite por Critical: 49

Score: 49
```

### Múltiplas severidades

```text
Critical: 1
High: 2
Medium: 3
Low: 4
```

Penalidades aproximadas:

```text
Critical: 18.00
High:     10.77
Medium:    4.65
Low:       1.69
Total:    35.11
```

Score matemático:

```text
100 - 35.11 = 64.89
Arredondado = 65
```

Como existe um finding Critical:

```text
Score final = 49
```

## Histórico

Cada análise armazenará:

```text
security_score
score_version
rule_set_version
```

Exemplo:

```text
Analysis #1 → 49
Analysis #2 → 74
Analysis #3 → 89
Analysis #4 → 99
```

A interface mostrará:

- score atual;
- score anterior;
- diferença em pontos;
- evolução no gráfico;
- versão das regras;
- versão da fórmula.

## Findings considerados

O score é calculado no momento em que a análise termina.

Todos os findings produzidos naquela execução participam do cálculo.

Alterar posteriormente um status para:

```text
RESOLVED
ACCEPTED_RISK
FALSE_POSITIVE
```

não modificará retroativamente o score salvo.

Para confirmar melhora no score, uma nova análise deverá ser executada.

Isso impede que o histórico mude sem alteração no código analisado.

## Comparações entre versões

Comparações são diretamente equivalentes quando possuem:

```text
mesmo score_version
mesmo rule_set_version
```

Se a fórmula ou as regras mudarem, a interface deverá avisar que a comparação utiliza versões diferentes.

## Implementação

O cálculo será implementado como um serviço de domínio sem dependência de:

- Spring;
- banco de dados;
- HTTP;
- frontend.

Planejamento:

```text
engine/scoring/
├── SecurityScoreCalculator.java
├── FindingCounts.java
├── SecurityScore.java
└── ScoreVersion.java
```

Isso permitirá testar a fórmula com testes unitários rápidos.

## Testes obrigatórios

| Cenário | Resultado |
|---|---:|
| Nenhum finding | 100 |
| Um Low | 99 |
| Um Medium | 89 |
| Um High | 74 |
| Um Critical | 49 |
| Score matemático abaixo de zero | 0 |
| Resultado decimal | Arredondado corretamente |
| Contagens negativas | Rejeitadas |
| Mesma entrada executada novamente | Mesmo resultado |

Também serão testadas combinações de severidades e os limites de cada faixa.

## Limitações

O Security Score representa as regras executadas pelo CodeFortress.

Ele não significa:

- ausência completa de vulnerabilidades;
- aprovação de auditoria;
- conformidade automática;
- substituição de revisão humana;
- classificação CVSS individual.

O produto deverá comunicar essas limitações claramente.
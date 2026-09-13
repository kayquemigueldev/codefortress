import {
    securityRules,
} from './security-rules-data'

export function SecurityRulesPage() {
    const activeRules =
        securityRules.filter(
            (rule) => rule.active,
        )

    const criticalRules =
        securityRules.filter(
            (rule) =>
                rule.severity === 'CRITICAL',
        ).length

    const highRules =
        securityRules.filter(
            (rule) =>
                rule.severity === 'HIGH',
        ).length

    const mediumRules =
        securityRules.filter(
            (rule) =>
                rule.severity === 'MEDIUM',
        ).length

    return (
        <main className="security-rules-page">
            <div className="security-rules-container">
                <header className="security-rules-header">
                    <div>
                        <p className="projects-eyebrow">
                            Security Engine
                        </p>

                        <h1>
                            Security Rules
                        </h1>

                        <p>
                            Review the static analysis rules
                            currently enabled in the
                            CodeFortress Security Engine.
                        </p>
                    </div>

                    <div className="security-rules-engine-status">
                        <span>
                            Engine status
                        </span>

                        <strong>
                            {activeRules.length}
                            {' '}
                            / 8 active
                        </strong>

                        <small>
                            Rule set v1.0.0
                        </small>
                    </div>
                </header>

                <section className="security-rules-summary">
                    <RuleSummary
                        label="Active rules"
                        value={activeRules.length}
                    />

                    <RuleSummary
                        label="Critical"
                        value={criticalRules}
                        severity="critical"
                    />

                    <RuleSummary
                        label="High"
                        value={highRules}
                        severity="high"
                    />

                    <RuleSummary
                        label="Medium"
                        value={mediumRules}
                        severity="medium"
                    />
                </section>

                <section className="security-rules-catalog">
                    <div className="security-rules-section-heading">
                        <div>
                            <p className="projects-eyebrow">
                                Rule catalog
                            </p>

                            <h2>
                                Detection coverage
                            </h2>
                        </div>

                        <span>
                            {securityRules.length}
                            {' '}
                            rules
                        </span>
                    </div>

                    <div className="security-rules-list">
                        {securityRules.map(
                            (rule) => (
                                <article
                                    className="security-rule-card"
                                    key={rule.key}
                                >
                                    <div className="security-rule-card__header">
                                        <div>
                                            <div className="security-rule-card__identity">
                                                <span className="security-rule-key">
                                                    {rule.key}
                                                </span>

                                                <span className="security-rule-version">
                                                    v
                                                    {rule.version}
                                                </span>
                                            </div>

                                            <h3>
                                                {rule.title}
                                            </h3>
                                        </div>

                                        <div className="security-rule-card__badges">
                                            <span
                                                className={
                                                    `security-rule-severity security-rule-severity--${rule.severity.toLowerCase()}`
                                                }
                                            >
                                                {rule.severity}
                                            </span>

                                            <span className="security-rule-category">
                                                {rule.category}
                                            </span>

                                            <span
                                                className={
                                                    rule.active
                                                        ? 'security-rule-status security-rule-status--active'
                                                        : 'security-rule-status'
                                                }
                                            >
                                                {rule.active
                                                    ? 'Active'
                                                    : 'Inactive'}
                                            </span>
                                        </div>
                                    </div>

                                    <p className="security-rule-description">
                                        {rule.description}
                                    </p>

                                    <div className="security-rule-details">
                                        <RuleDetail
                                            label="Impact"
                                            content={
                                                rule.impact
                                            }
                                        />

                                        <RuleDetail
                                            label="Recommendation"
                                            content={
                                                rule.recommendation
                                            }
                                        />
                                    </div>
                                </article>
                            ),
                        )}
                    </div>
                </section>
            </div>
        </main>
    )
}

function RuleSummary({
                         label,
                         value,
                         severity,
                     }: {
    label: string
    value: number
    severity?:
        | 'critical'
        | 'high'
        | 'medium'
}) {
    const className = [
        'security-rules-summary-card__value',
        severity
            ? `security-rules-summary-card__value--${severity}`
            : '',
    ]
        .filter(Boolean)
        .join(' ')

    return (
        <article className="security-rules-summary-card">
            <span>
                {label}
            </span>

            <strong className={className}>
                {value}
            </strong>
        </article>
    )
}

function RuleDetail({
                        label,
                        content,
                    }: {
    label: string
    content: string
}) {
    return (
        <div className="security-rule-detail">
            <span>
                {label}
            </span>

            <p>
                {content}
            </p>
        </div>
    )
}
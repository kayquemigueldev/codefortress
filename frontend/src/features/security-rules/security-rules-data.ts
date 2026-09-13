export type SecurityRuleSeverity =
    | 'CRITICAL'
    | 'HIGH'
    | 'MEDIUM'
    | 'LOW'

export type SecurityRuleCategory =
    | 'SECRETS'
    | 'CONFIGURATION'
    | 'CODE'

export type SecurityRuleCatalogItem = {
    key: string
    version: string
    title: string
    category: SecurityRuleCategory
    severity: SecurityRuleSeverity
    description: string
    impact: string
    recommendation: string
    active: boolean
}

export const securityRules:
    SecurityRuleCatalogItem[] = [
    {
        key: 'CF-SEC-001',
        version: '1.0.0',
        title: 'Hardcoded Secret',
        category: 'SECRETS',
        severity: 'CRITICAL',
        description:
            'Detects passwords, tokens, API keys, and other secrets embedded directly in source code or configuration.',
        impact:
            'Exposed secrets may allow unauthorized access to systems, services, or sensitive data.',
        recommendation:
            'Move secrets to environment variables or a dedicated secret manager and rotate exposed credentials.',
        active: true,
    },
    {
        key: 'CF-SEC-002',
        version: '1.0.0',
        title: 'Debug Mode Enabled',
        category: 'CONFIGURATION',
        severity: 'MEDIUM',
        description:
            'Detects application debug mode explicitly enabled in configuration files.',
        impact:
            'Debug mode may expose stack traces, internal application details, configuration data, or other sensitive information.',
        recommendation:
            'Disable debug mode in production and enable it only in controlled development environments.',
        active: true,
    },
    {
        key: 'CF-SEC-003',
        version: '1.0.0',
        title: 'TLS Certificate Verification Disabled',
        category: 'CONFIGURATION',
        severity: 'HIGH',
        description:
            'Detects configuration that disables TLS or SSL certificate verification.',
        impact:
            'Disabling certificate verification may allow attackers to intercept or impersonate trusted services through man-in-the-middle attacks.',
        recommendation:
            'Enable TLS certificate verification and use certificates issued by a trusted certificate authority.',
        active: true,
    },
    {
        key: 'CF-SEC-004',
        version: '1.0.0',
        title: 'Permissive CORS Configuration',
        category: 'CONFIGURATION',
        severity: 'MEDIUM',
        description:
            'Detects CORS configuration that allows requests from any origin.',
        impact:
            'Allowing every origin may expose application resources to untrusted websites and increase the risk of unauthorized cross-origin access.',
        recommendation:
            'Restrict allowed CORS origins to explicitly trusted domains instead of using a wildcard.',
        active: true,
    },
    {
        key: 'CF-SEC-005',
        version: '1.0.0',
        title: 'SQL Injection Risk',
        category: 'CODE',
        severity: 'HIGH',
        description:
            'Detects SQL statements constructed through direct string concatenation with variable data.',
        impact:
            'Building SQL queries with untrusted values may allow attackers to alter the intended query and access, modify, or delete sensitive data.',
        recommendation:
            'Use parameterized queries, prepared statements, or ORM query parameters instead of concatenating values directly into SQL statements.',
        active: true,
    },
    {
        key: 'CF-SEC-006',
        version: '1.0.0',
        title: 'Weak Cryptographic Hash Algorithm',
        category: 'CODE',
        severity: 'MEDIUM',
        description:
            'Detects the use of cryptographically weak hash algorithms such as MD5 and SHA-1.',
        impact:
            'Weak hash algorithms are vulnerable to collision attacks and should not be used for security-sensitive hashing, signatures, integrity checks, or credential protection.',
        recommendation:
            'Use a modern cryptographic hash algorithm such as SHA-256 or SHA-512. For password storage, use a dedicated password hashing algorithm such as Argon2, bcrypt, or scrypt.',
        active: true,
    },
    {
        key: 'CF-SEC-007',
        version: '1.0.0',
        title: 'Sensitive Information Exposure',
        category: 'CODE',
        severity: 'HIGH',
        description:
            'Detects potentially sensitive values being written to application logs or standard output.',
        impact:
            'Exposing passwords, tokens, secrets, API keys, or credentials in logs may allow unauthorized users to obtain sensitive information.',
        recommendation:
            'Avoid logging sensitive values. Remove them from log statements or mask and redact the data before it is written to logs.',
        active: true,
    },
    {
        key: 'CF-SEC-008',
        version: '1.0.0',
        title: 'Exposed Management Endpoints',
        category: 'CONFIGURATION',
        severity: 'HIGH',
        description:
            'Detects management endpoint configuration that exposes all available web endpoints.',
        impact:
            'Exposing every management endpoint may reveal sensitive operational information or administrative functionality to unintended users.',
        recommendation:
            'Expose only the management endpoints that are required, such as health and info, and protect sensitive endpoints with appropriate authentication and network restrictions.',
        active: true,
    },
]
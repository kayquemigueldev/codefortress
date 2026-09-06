export type FindingSeverity =
    | 'CRITICAL'
    | 'HIGH'
    | 'MEDIUM'
    | 'LOW'

export type FindingCategory =
    | 'SECRETS'
    | 'CONFIGURATION'
    | 'CODE'
    | 'DEPENDENCY'

export type FindingStatus =
    | 'OPEN'
    | 'RESOLVED'
    | 'ACCEPTED_RISK'
    | 'FALSE_POSITIVE'

export type Finding = {
    id: string
    ruleKey: string
    ruleVersion: string
    title: string
    category: FindingCategory
    severity: FindingSeverity
    status: FindingStatus
    filePath: string
    startLine: number
    endLine: number
    codeExcerpt: string
    description: string
    impact: string
    recommendation: string
    fingerprint: string
    createdAt: string
    statusUpdatedAt: string
}
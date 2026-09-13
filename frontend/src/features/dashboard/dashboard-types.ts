import type {
    AnalysisStatus,
} from '../analyses/analysis-types'

export type DashboardLatestAnalysis = {
    id: string
    projectId: string
    projectName: string
    sequenceNumber: number
    status: AnalysisStatus
    sourceFilename: string
    securityScore: number | null
    findingsCount: number | null
    createdAt: string
    completedAt: string | null
}

export type DashboardOverview = {
    activeProjects: number
    averageSecurityScore: number | null
    openFindings: number
    criticalOpenFindings: number
    highOpenFindings: number
    mediumOpenFindings: number
    lowOpenFindings: number
    latestAnalysis: DashboardLatestAnalysis | null
    recentAnalyses: DashboardLatestAnalysis[]
}
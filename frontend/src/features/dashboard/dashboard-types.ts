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
    openFindings: number
    criticalOpenFindings: number
    latestAnalysis: DashboardLatestAnalysis | null
}
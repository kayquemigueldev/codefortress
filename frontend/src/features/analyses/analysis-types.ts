export type AnalysisStatus =
    | 'QUEUED'
    | 'RUNNING'
    | 'COMPLETED'
    | 'FAILED'
    | 'CANCELLED'

export type AnalysisSourceType =
    | 'UPLOAD'

export type Analysis = {
    id: string
    sequenceNumber: number
    status: AnalysisStatus
    sourceType: AnalysisSourceType
    sourceFilename: string
    securityScore: number | null
    filesScanned: number | null
    linesScanned: number | null
    findingsCount: number | null
    startedAt: string | null
    completedAt: string | null
    createdAt: string
}

export type UploadedAnalysis = {
    id: string
    projectId: string
    sequenceNumber: number
    status: AnalysisStatus
    sourceType: AnalysisSourceType
    sourceFilename: string
    sourceSizeBytes: number
    createdAt: string
}
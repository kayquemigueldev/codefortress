import {
    authenticatedApiRequest,
} from '../../lib/api/api-client'

import type {
    Finding,
} from './finding-types'

export function listFindings(
    projectId: string,
    analysisId: string,
) {
    return authenticatedApiRequest<Finding[]>(
        `/projects/${projectId}/analyses/${analysisId}/findings`,
    )
}
import {
    authenticatedApiRequest,
} from '../../lib/api/api-client'

import type {
    Finding,
    FindingStatus,
} from './finding-types'

export function listFindings(
    projectId: string,
    analysisId: string,
) {
    return authenticatedApiRequest<Finding[]>(
        `/projects/${projectId}/analyses/${analysisId}/findings`,
    )
}

export function updateFindingStatus(
    projectId: string,
    analysisId: string,
    findingId: string,
    status: FindingStatus,
) {
    return authenticatedApiRequest<Finding>(
        `/projects/${projectId}/analyses/${analysisId}/findings/${findingId}/status`,
        {
            method: 'PATCH',

            headers: {
                'Content-Type':
                    'application/json',
            },

            body: JSON.stringify({
                status,
            }),
        },
    )
}
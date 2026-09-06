import {
    authenticatedApiRequest,
} from '../../lib/api/api-client'

import type {
    DashboardOverview,
} from './dashboard-types'

export function getDashboardOverview() {
    return authenticatedApiRequest<DashboardOverview>(
        '/dashboard',
    )
}
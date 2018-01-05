// @flow

import type {Dashboard, State as DashboardState} from '../flowtype/dashboard-types'

export const parseDashboard = ({dashboard: {dashboardsById, openDashboard}}: {dashboard: DashboardState}): {
    dashboard: Dashboard,
    canShare: boolean,
    canWrite: boolean
} => {
    const db = dashboardsById[openDashboard.id] || {}
    return {
        dashboard: db,
        canShare: db.new !== true && (db.ownPermissions || []).includes('share'),
        canWrite: db.new === true || (db.ownPermissions || []).includes('write')
    }
}
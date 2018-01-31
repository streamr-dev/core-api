// @flow

import type {DashboardState} from '../flowtype/states/dashboard-state'
import type {Dashboard} from '../flowtype/dashboard-types'

export const parseDashboard = ({dashboard: {dashboardsById, openDashboard}}: {dashboard: DashboardState}): {
    dashboard: Dashboard | {},
    canShare: boolean,
    canWrite: boolean
} => {
    const db = openDashboard.id && dashboardsById[openDashboard.id] || {}
    return {
        dashboard: db,
        canShare: db.new !== true && (db.ownPermissions || []).includes('share'),
        canWrite: db.new === true || (db.ownPermissions || []).includes('write')
    }
}

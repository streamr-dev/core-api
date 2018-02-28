// @flow

import type {Dashboard} from '../dashboard-types.js'
import type {ErrorInUi} from '../common-types'

export type DashboardState = {
    dashboardsById: {
        [$ElementType<Dashboard, 'id'>]: Dashboard
    },
    openDashboard: {
        id: ?$ElementType<Dashboard, 'id'>,
        isFullScreen: boolean
    },
    error: ?ErrorInUi,
    fetching: boolean
}

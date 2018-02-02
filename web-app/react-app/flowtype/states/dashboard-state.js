// @flow

import type {Dashboard} from '../dashboard-types.js'
import type {ApiError} from '../common-types'

export type DashboardState = {
    dashboardsById: {
        [$ElementType<Dashboard, 'id'>]: Dashboard
    },
    openDashboard: {
        id: ?$ElementType<Dashboard, 'id'>,
        isFullScreen: boolean
    },
    error: ?ApiError,
    fetching: boolean
}

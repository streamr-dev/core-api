
import type {Canvas} from './canvas-types'
import type {Permission} from './permission-types'

export type Dashboard = {
    id: string,
    name: string,
    items: Array<DashboardItem>,
    ownPermissions?: Array<Permission.operation>,
    editingLocked?: boolean,
    new?: boolean
}

export type DashboardReducerState = {
    dashboardsById?: {
        [Dashboard.id]: Dashboard
    },
    openDashboard: {
        id: ?Dashboard.id,
        isFullScreen: boolean
    },
    error?: ?string,
    fetching?: boolean,
    saved: boolean,
    new: boolean
}

export type DashboardReducerAction = {
    type: string,
    id?: Dashboard.id,
    dashboard?: Dashboard,
    dashboards?: Array<Dashboard>,
    error?: string
}

export type DashboardItem = {
    id: ?string,
    title: string,
    dashboard: Dashboard.id,
    module: number,
    canvas: Canvas,
    size: string,
    ord: number,
    layout?: {
        x: number,
        y: number,
        w: number,
        h: number,
        static?: boolean,
        minW?: number,
        minH?: number
    }
}

import type {Canvas} from './canvas-types'

export type Dashboard = {
    id: string,
    name: string,
    items: Array<DashboardItem>,
    permissions?: Array<string>,
    editingLocked?: boolean
}

export type DashboardReducerState = {
    dashboardsById?: {
        [Dashboard.id]: Dashboard
    },
    openDashboard: {
        id: ?Dashboard.id
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
    id: ?number,
    tempId?: number,
    title: string,
    dashboard: Dashboard.id,
    module: number,
    canvas: Canvas.id,
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

import type {Canvas} from './canvas-types'

export type Dashboard = {
    id: number,
    name: string,
    items: Array<DashboardItem>,
    permissions?: Array<string>
}

export type DashboardReducerState = {
    dashboardsById?: {
        [number]: Dashboard
    },
    error?: ?string,
    fetching?: boolean
}

export type DashboardReducerAction = {
    type: string,
    id?: number,
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
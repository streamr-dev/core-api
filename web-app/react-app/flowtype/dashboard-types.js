// @flow

import type {Canvas} from './canvas-types'
import type {Permission} from './permission-types'
import type {Webcomponent} from './webcomponent-types'

export type Dashboard = {
    id: string,
    name: string,
    items: Array<DashboardItem>,
    ownPermissions?: Array<$ElementType<Permission, 'operation'>>,
    editingLocked?: boolean,
    layout: Layout,
    new?: boolean
}

export type LayoutItem = {
    i: string | number,
    h: number,
    isDraggable: ?number,
    isResizable: ?number,
    maxH: ?number,
    maxW: ?number,
    minH: number,
    minW: number,
    moved: boolean,
    static: boolean,
    w: number,
    x: number,
    y: number
}

export type Layout = {
    xs?: Array<LayoutItem>,
    sm?: Array<LayoutItem>,
    md?: Array<LayoutItem>,
    lg?: Array<LayoutItem>
}

export type DashboardItem = {
    id: ?string,
    title: string,
    dashboard: ?$ElementType<Dashboard, 'id'>,
    module: number,
    canvas: $ElementType<Canvas, 'id'>,
    layout?: Layout,
    webcomponent: $ElementType<Webcomponent, 'type'>
}

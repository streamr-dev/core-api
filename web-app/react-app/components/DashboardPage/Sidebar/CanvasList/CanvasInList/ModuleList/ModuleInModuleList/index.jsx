// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'

import {addDashboardItem, removeDashboardItem} from '../../../../../../../actions/dashboard'

import styles from './moduleInModuleList.pcss'
import uuid from 'uuid'

import type {DashboardState} from '../../../../../../../flowtype/states/dashboard-state'
import type { Dashboard, DashboardItem } from '../../../../../../../flowtype/dashboard-types'
import type { Canvas, CanvasModule } from '../../../../../../../flowtype/canvas-types'

type StateProps = {
    dashboard: ?Dashboard,
    checked: boolean
}

type DispatchProps = {
    addDashboardItem: (dashboard: Dashboard, item: DashboardItem) => void,
    removeDashboardItem: (dashboard: Dashboard, item: DashboardItem) => void
}

type GivenProps = {
    module: CanvasModule,
    canvasId: $ElementType<Canvas, 'id'>,
    dispatch: Function,
    id: $ElementType<Dashboard, 'id'>
}

type Props = StateProps & DispatchProps & GivenProps

export class ModuleInModuleList extends Component<Props> {
    
    onClick = () => {
        const id = uuid.v4()
        const dbItem: DashboardItem = {
            id,
            dashboard: this.props.dashboard && this.props.dashboard.id,
            module: this.props.module.hash,
            canvas: this.props.canvasId,
            webcomponent: this.props.module.uiChannel.webcomponent,
            title: this.props.module.name
        }
        if (this.props.checked) {
            this.props.dashboard && this.props.removeDashboardItem(this.props.dashboard, dbItem)
        } else {
            this.props.dashboard && this.props.addDashboardItem(this.props.dashboard, dbItem)
        }
    }
    
    render() {
        const {module, checked} = this.props
        return (
            <li className="module" onClick={this.onClick}>
                <a href="#" className={`${styles.module} ${checked ? styles.checked : ''}`}>
                    <FontAwesome name={checked ? 'check-square' : 'square'} className={styles.checkIcon}/>
                    {module.name}
                </a>
            </li>
        )
    }
}

export const mapStateToProps = ({dashboard}: {dashboard: DashboardState}, ownProps: GivenProps): StateProps => {
    const db = dashboard.openDashboard.id && dashboard.dashboardsById[dashboard.openDashboard.id] || null
    return {
        dashboard: db,
        checked: db && db.items ? db.items.find(item => item.canvas === ownProps.canvasId && item.module === ownProps.module.hash) !== undefined : false
    }
}

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    addDashboardItem(dashboard: Dashboard, item: DashboardItem) {
        dispatch(addDashboardItem(dashboard, item))
    },
    removeDashboardItem(dashboard: Dashboard, item: DashboardItem) {
        dispatch(removeDashboardItem(dashboard, item))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ModuleInModuleList)
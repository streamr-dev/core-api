// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'

import {addDashboardItem, removeDashboardItem} from '../../../../../../../actions/dashboard'

import styles from './moduleInModuleList.pcss'
import uuid from 'uuid'

import type { Dashboard, DashboardItem } from '../../../../../../../flowtype/dashboard-types'
import type { Canvas, CanvasModule } from '../../../../../../../flowtype/canvas-types'

type Props = {
    dashboard: Dashboard,
    module: CanvasModule,
    canvasId: Canvas.id,
    checked: boolean,
    dispatch: Function,
    id: Dashboard.id
}

class ModuleInModuleList extends Component<Props> {
    
    onClick = () => {
        const id = uuid.v4()
        const dbItem: DashboardItem = {
            id,
            dashboard: this.props.dashboard.id,
            module: this.props.module.hash,
            canvas: this.props.canvasId,
            webcomponent: this.props.module.uiChannel.webcomponent,
            size: 'small',
            ord: 0,
            title: this.props.module.name
        }
        if (this.props.checked) {
            this.props.dispatch(removeDashboardItem(this.props.dashboard, dbItem))
        } else {
            this.props.dispatch(addDashboardItem(this.props.dashboard, dbItem))
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

const mapStateToProps = ({dashboard}, ownProps) => {
    const dbState = dashboard
    const db = dbState.dashboardsById[dbState.openDashboard.id] || {}
    return {
        dashboard: db,
        checked: db && db.items ? db.items.find(item => item.canvas === ownProps.canvasId && item.module === ownProps.module.hash) !== undefined : false
    }
}

export default connect(mapStateToProps)(ModuleInModuleList)
// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'

import {addDashboardItem, removeDashboardItem} from '../../../../../../../actions/dashboard'

import styles from './moduleInModuleList.pcss'
import uuid from 'uuid'

import type { Dashboard, DashboardItem } from '../../../../../../../flowtype/dashboard-types'
import type { Canvas, CanvasModule } from '../../../../../../../flowtype/canvas-types'

class ModuleInModuleList extends Component {
    
    onClick: Function
    
    props: {
        dashboard: Dashboard,
        module: CanvasModule,
        canvasId: Canvas.id,
        checked: boolean,
        dispatch: Function,
        id: Dashboard.id
    }
    
    constructor() {
        super()
        this.onClick = this.onClick.bind(this)
    }
    
    onClick() {
        const dbItem: DashboardItem = {
            id: uuid.v4(),
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
            <li key={module.id} className="module" onClick={this.onClick}>
                <a href="#" className={`${styles.module} ${checked ? styles.checked : ''}`}>
                    <FontAwesome name={checked ? 'check-square' : 'square'} className={styles.checkIcon}/>
                    {module.name}
                </a>
            </li>
        )
    }
}

const mapStateToProps = ({dashboard}, ownProps) => {
    const db = dashboard.dashboardsById[dashboard.openDashboard.id] || {}
    return {
        dashboard: db,
        checked: db && db.items ? db.items.find(item => item.canvas === ownProps.canvasId && item.module === ownProps.module.hash) !== undefined : false
    }
}

export default connect(mapStateToProps)(ModuleInModuleList)
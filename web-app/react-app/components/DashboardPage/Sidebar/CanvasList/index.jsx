// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import CanvasInList from './CanvasInList'

import styles from './canvasList.pcss'

import type {Canvas} from '../../../../flowtype/canvas-types'
import type {CanvasState} from '../../../../flowtype/states/canvas-state'
import type {DashboardState} from '../../../../flowtype/states/dashboard-state'

type StateProps = {
    canvases: Array<Canvas>,
    showCanvases: boolean
}

type Props = StateProps

export class CanvasList extends Component<Props> {
    render() {
        return this.props.showCanvases ? (
            <ul className="navigation">
                <li className={styles.canvasListTitle}>
                    <label>Running Canvases</label>
                </li>
                {this.props.canvases.map(canvas => (
                    <CanvasInList key={canvas.id} canvas={canvas}/>
                ))}
            </ul>
        ) : null
    }
}

export const mapStateToProps = ({canvas, dashboard}: {canvas: CanvasState, dashboard: DashboardState}): StateProps => {
    const db = dashboard.openDashboard.id && dashboard.dashboardsById[dashboard.openDashboard.id] || {}
    const canWrite = db.ownPermissions ? db.ownPermissions.includes('write') : false
    return {
        canvases: canvas.list || [],
        showCanvases: db.new || canWrite
    }
}

export default connect(mapStateToProps)(CanvasList)

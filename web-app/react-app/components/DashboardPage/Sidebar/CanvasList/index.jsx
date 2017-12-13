// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import CanvasInList from './CanvasInList'

import styles from './canvasList.pcss'

import type {Canvas} from '../../../../flowtype/canvas-types'

type Props = {
    canvases: Array<Canvas>,
    showCanvases: boolean,
}

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

export const mapStateToProps = ({canvas, dashboard}) => {
    const dbState = dashboard
    const db = dbState.dashboardsById[dbState.openDashboard.id] || {}
    const canWrite = db.ownPermissions && db.ownPermissions.includes('write')
    return {
        canvases: canvas.list || [],
        showCanvases: db.new || canWrite
    }
}

export default connect(mapStateToProps)(CanvasList)
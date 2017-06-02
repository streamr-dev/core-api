// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'

import NameEditor from './NameEditor'
import CanvasList from './CanvasList'
import DashboardTools from './DashboardTools'

import type { Dashboard } from '../../../flowtype/dashboard-types'
import type { Canvas } from '../../../flowtype/canvas-types'

import styles from './sidebar.pcss'

class Sidebar extends Component {
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>
    }
    
    render() {
        return (
            <div id="main-menu" role="navigation" className={styles.sidebar}>
                <div id="main-menu-inner">
                    <div id="sidebar-view" className="scrollable">
                        <NameEditor dashboard={this.props.dashboard}/>
                        <CanvasList canvases={this.props.canvases}/>
                        <DashboardTools dashboard={this.props.dashboard}/>
                    </div>
                </div>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard, canvas}) => ({
    canvases: canvas.list,
    error: dashboard.error
})

export default connect(mapStateToProps, null)(Sidebar)
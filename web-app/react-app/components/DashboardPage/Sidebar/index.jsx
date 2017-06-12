// @flux

import React, {Component} from 'react'

import NameEditor from './NameEditor'
import CanvasList from './CanvasList'
import DashboardTools from './DashboardTools'

import type { Dashboard } from '../../../flowtype/dashboard-types'
import type { Canvas } from '../../../flowtype/canvas-types'

import styles from './sidebar.pcss'

export default class Sidebar extends Component {
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>
    }
    
    render() {
        return (
            <div id="main-menu" role="navigation" className={styles.sidebar}>
                <div id="main-menu-inner">
                    <div id="sidebar-view" className="scrollable">
                        <NameEditor/>
                        <CanvasList/>
                        <DashboardTools/>
                    </div>
                </div>
            </div>
        )
    }
}
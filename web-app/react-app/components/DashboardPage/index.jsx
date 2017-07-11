// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Helmet} from 'react-helmet'

import Notifier from '../Notifier'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'

import type { Dashboard } from '../../flowtype/dashboard-types'
import type { Canvas } from '../../flowtype/canvas-types'
import type {ReactChildren} from 'react-flow-types'

class DashboardPage extends Component {
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>,
        children: ReactChildren
    }
    
    render() {
        return (
            <div style={{
                height: '100%'
            }}>
                <Helmet>
                    <title>{this.props.dashboard && this.props.dashboard.name || 'New Dashboard'}</title>
                </Helmet>
                <Notifier/>
                <Sidebar/>
                <Editor/>
                {this.props.children}
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => ({
    dashboard: dashboard.dashboardsById[dashboard.openDashboard.id]
})

export default connect(mapStateToProps, null)(DashboardPage)
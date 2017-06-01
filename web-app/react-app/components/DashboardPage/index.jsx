// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import uuid from 'uuid'

import {Row} from 'react-bootstrap'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'

import {getDashboard, getMyDashboardPermissions, newDashboard, openDashboard} from '../../actions/dashboard'
import {getRunningCanvases} from '../../actions/canvas'

import type { Dashboard } from '../../types/dashboard-types'
import type { Canvas } from '../../types/canvas-types'

declare var DASHBOARD_ID: Dashboard.id

class DashboardPage extends Component {
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>,
        dispatch: Function,
        error: {
            message: string
        },
        fetching: boolean
    }
    
    componentDidMount() {
        const id = DASHBOARD_ID || uuid.v4()
        if (DASHBOARD_ID) {
            this.props.dispatch(getDashboard(id))
            this.props.dispatch(getMyDashboardPermissions(id))
        } else {
            this.props.dispatch(newDashboard(id))
        }
        this.props.dispatch(getRunningCanvases())
        this.props.dispatch(openDashboard(id))
    }
    
    render() {
        return (
            <Row style={{
                height: '100%'
            }}>
                <Sidebar dashboard={this.props.dashboard}/>
                <Editor dashboard={this.props.dashboard}/>
            </Row>
        )
    }
}

const mapStateToProps = ({dashboard}) => ({
    dashboard: dashboard.dashboardsById[dashboard.openDashboard],
    fetching: dashboard.fetching,
    error: dashboard.error
})

export default connect(mapStateToProps, null)(DashboardPage)
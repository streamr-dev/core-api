// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {Row} from 'react-bootstrap'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'

import {getDashboard, getMyDashboardPermissions} from '../../actions/dashboard'
import {getRunningCanvases} from '../../actions/canvas'

import type { Dashboard } from '../../types/dashboard-types'
import type { Canvas } from '../../types/canvas-types'

// TODO: find a better way
const id = parseFloat(window.location.href.split('/dashboard/showNew/')[1])

class DashboardPage extends Component {
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>,
        dispatch: Function
    }
    
    componentDidMount() {
        this.props.dispatch(getDashboard(id))
        this.props.dispatch(getMyDashboardPermissions(id))
        this.props.dispatch(getRunningCanvases())
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
    dashboard: dashboard.dashboardsById[id],
    error: dashboard.error
})

export default connect(mapStateToProps, null)(DashboardPage)
// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Helmet} from 'react-helmet'
import Notifier from '../StreamrNotifierWrapper'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'
import uuid from 'uuid'

import {getDashboard, getMyDashboardPermissions, newDashboard, openDashboard} from '../../actions/dashboard'
import {getRunningCanvases} from '../../actions/canvas'


import type { DashboardState } from '../../flowtype/states/dashboard-state'
import type { Dashboard } from '../../flowtype/dashboard-types'
import type { Canvas } from '../../flowtype/canvas-types'
import type {Node} from 'react'

import styles from './dashboardPage.pcss'

type StateProps = {
    dashboard: ?Dashboard
}

type DispatchProps = {
    getDashboard: (id: string) => void,
    getMyDashboardPermissions: (id: string) => void,
    newDashboard: (id: string) => void,
    getRunningCanvases: () => void,
    openDashboard: (id: string) => void
}

type GivenProps = {
    canvases: Array<Canvas>,
    children: Node | Array<Node>
}

type RouterProps = {
    match: {
        params: {
            id?: string
        }
    }
}

type Props = StateProps & DispatchProps & GivenProps & RouterProps

export class DashboardPage extends Component<Props> {
    
    componentWillMount() {
        let id = this.props.match.params.id
        if (id !== undefined) {
            this.props.getDashboard(id)
            this.props.getMyDashboardPermissions(id)
        } else {
            id = uuid.v4()
            this.props.newDashboard(id)
        }
        this.props.getRunningCanvases()
        this.props.openDashboard(id)
    }
    
    render() {
        return (
            <div className={styles.dashboardPage}>
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

export const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}: {dashboard: DashboardState}): StateProps => ({
    dashboard: openDashboard.id ? dashboardsById[openDashboard.id] : null
})

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    getDashboard(id: string) {
        dispatch(getDashboard(id))
    },
    getMyDashboardPermissions(id: string) {
        dispatch(getMyDashboardPermissions(id))
    },
    newDashboard(id: string) {
        dispatch(newDashboard(id))
    },
    getRunningCanvases() {
        dispatch(getRunningCanvases())
    },
    openDashboard(id: string) {
        dispatch(openDashboard(id))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardPage)
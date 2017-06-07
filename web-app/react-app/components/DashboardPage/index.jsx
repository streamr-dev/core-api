// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Helmet} from 'react-helmet'

import Notifier from '../Notifier'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'

import type { Dashboard } from '../../flowtype/dashboard-types'
import type { Canvas } from '../../flowtype/canvas-types'

declare var Streamr: {
    showError: () => void
}

class DashboardPage extends Component {
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>,
        dispatch: Function,
        error: {
            message: string
        },
        fetching: boolean,
        params: any
    }

    componentWillReceiveProps(props) {
        if (props.error && props.error !== this.props.error) {
            Streamr.showError(props.error)
        }
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
                <Sidebar dashboard={this.props.dashboard}/>
                <Editor dashboard={this.props.dashboard}/>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => {
    const db = dashboard.dashboardsById[dashboard.openDashboard.id]
    return {
        dashboard: db && {
            ...db,
            items: db.items
        },
        fetching: dashboard.fetching,
        error: dashboard.error
    }
}

export default connect(mapStateToProps, null)(DashboardPage)
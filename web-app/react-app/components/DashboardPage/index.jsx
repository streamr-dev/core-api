// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {Row} from 'react-bootstrap'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'

import {getDashboard} from '../../actions/dashboard'

type Dashboard = {
    id: number,
    name: string,
    items: Array<{}>
}

// Better way
const id = parseFloat(window.location.href.split('/dashboard/showNew/')[1])

class DashboardPage extends Component {
    
    props: {
        dashboard: Dashboard,
        dispatch: Function
    }
    
    componentDidMount() {
        this.props.dispatch(getDashboard(id))
    }
    
    render() {
        return (
            <Row>
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
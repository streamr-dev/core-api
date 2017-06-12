// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {FormControl} from 'react-bootstrap'

import {updateDashboard} from '../../../../actions/dashboard'

import type {Dashboard} from '../../../../flowtype/dashboard-types'

class NameEditor extends Component {
    
    onChange: Function
    
    props: {
        dashboard: Dashboard,
        dispatch: Function
    }
    
    constructor() {
        super()
        this.onChange = this.onChange.bind(this)
    }
    
    onChange({target}) {
        this.props.dispatch(updateDashboard({
            ...this.props.dashboard,
            name: target.value
        }))
    }
    
    render() {
        return (
            <div className="menu-content">
                <label>
                    Dashboard Name
                </label>
                <FormControl
                    type="text"
                    className="dashboard-name title-input"
                    name="dashboard-name"
                    value={this.props.dashboard && this.props.dashboard.name || ''}
                    onChange={this.onChange}
                />
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => ({
    dashboard: dashboard.dashboardsById[dashboard.openDashboard.id]
})

export default connect(mapStateToProps)(NameEditor)
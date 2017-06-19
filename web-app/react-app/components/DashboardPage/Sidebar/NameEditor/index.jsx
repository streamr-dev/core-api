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
        update: Function,
        isDisabled: ?boolean
    }
    
    constructor() {
        super()
        this.onChange = this.onChange.bind(this)
    }
    
    onChange({target}) {
        this.props.update({
            ...this.props.dashboard,
            name: target.value
        })
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
                    disabled={this.props.isDisabled}
                />
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => {
    const db = dashboard.dashboardsById[dashboard.openDashboard.id] || {}
    return {
        dashboard: db,
        isDisabled: !db.new && (!db.ownPermissions || !db.ownPermissions.includes('write'))
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    update(newData) {
        return dispatch(updateDashboard({
            ...ownProps.dashboard,
            ...newData
        }))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(NameEditor)
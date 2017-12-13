// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {FormControl} from 'react-bootstrap'
import {parseDashboard} from '../../../../helpers/parseState'

import {updateDashboard} from '../../../../actions/dashboard'

import type {Dashboard} from '../../../../flowtype/dashboard-types'

type Props = {
    dashboard: Dashboard,
    update: Function,
    canWrite?: boolean
}

class NameEditor extends Component<Props> {
    
    onChange = ({target}) => {
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
                    disabled={!this.props.canWrite}
                />
            </div>
        )
    }
}

const mapStateToProps = (state) => parseDashboard(state)

const mapDispatchToProps = (dispatch) => ({
    update(newData) {
        return dispatch(updateDashboard({
            ...newData
        }))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(NameEditor)
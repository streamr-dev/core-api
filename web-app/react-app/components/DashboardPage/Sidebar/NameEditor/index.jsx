// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {FormControl} from 'react-bootstrap'
import {parseDashboard} from '../../../../helpers/parseState'

import {updateDashboard} from '../../../../actions/dashboard'

import type {Dashboard, DashboardReducerState as DashboardState} from '../../../../flowtype/dashboard-types'

import styles from './nameEditor.pcss'

type Props = {
    dashboard: Dashboard,
    update: Function,
    canWrite?: boolean
}

export class NameEditor extends Component<Props> {
    
    onChange = ({target}: {target: {value: string}}) => {
        this.props.update({
            ...this.props.dashboard,
            name: target.value
        })
    }
    
    render() {
        return (
            <div className={`menu-content ${styles.nameEditor}`}>
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

export const mapStateToProps = (state: {dashboard: DashboardState}) => parseDashboard(state)

export const mapDispatchToProps = (dispatch: Function) => ({
    update(dashboard: Dashboard) {
        return dispatch(updateDashboard(dashboard))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(NameEditor)
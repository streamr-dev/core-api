// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import ConfirmButton from '../../ConfirmButton'
import createLink from '../../../helpers/createLink'

import {deleteDashboard} from '../../../actions/dashboard'
import {parseDashboard} from '../../../helpers/parseState'

import type {Node} from 'react'
import type {DashboardState} from '../../../flowtype/states/dashboard-state'
import type {Dashboard} from '../../../flowtype/dashboard-types'

type Props = {
    dashboard: Dashboard,
    canWrite: boolean,
    buttonProps: {},
    children?: Node | Array<Node>,
    deleteDashboard: (id: $ElementType<Dashboard, 'id'>) => Promise<any>,
    className: string
}

export class DashboardDeleteButton extends Component<Props> {
    
    static defaultProps = {
        buttonProps: {},
        className: ''
    }
    
    onDelete = () => {
        this.props.deleteDashboard(this.props.dashboard.id)
            .then(() => {
                // TODO: change to be handled with react-router
                window.location.assign(createLink('/dashboard/list'))
            })
    }
    
    render() {
        return (
            <ConfirmButton
                buttonProps={{
                    disabled: !this.props.canWrite || this.props.dashboard.new,
                    ...this.props.buttonProps
                }}
                className={this.props.className}
                confirmCallback={this.onDelete}
                confirmTitle="Are you sure?"
                confirmMessage={`Are you sure you want to remove dashboard ${this.props.dashboard.name}?`}
            >
                {this.props.children}
            </ConfirmButton>
        )
    }
}

export const mapStateToProps = (state: {dashboard: DashboardState}) => parseDashboard(state)

export const mapDispatchToProps = (dispatch: Function) => ({
    deleteDashboard(id: $ElementType<Dashboard, 'id'>) {
        return dispatch(deleteDashboard(id))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardDeleteButton)
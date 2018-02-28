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

type StateProps = {
    dashboard: ?Dashboard,
    canWrite: boolean,
    canShare: boolean
}

type DispatchProps = {
    deleteDashboard: (id: $ElementType<Dashboard, 'id'>) => Promise<any>
}

type GivenProps = {
    buttonProps: {},
    children?: Node | Array<Node>,
    className: string
}

type Props = StateProps & DispatchProps & GivenProps

export class DashboardDeleteButton extends Component<Props> {

    static defaultProps = {
        buttonProps: {},
        className: '',
    }

    onDelete = () => {
        const {dashboard, deleteDashboard} = this.props
        dashboard && deleteDashboard(dashboard.id)
            .then(() => {
                // TODO: change to be handled with react-router
                window.location.assign(createLink('/dashboard/list'))
            })
    }

    render() {
        return (
            <ConfirmButton
                buttonProps={{
                    disabled: this.props.dashboard && (!this.props.canWrite || this.props.dashboard.new),
                    ...this.props.buttonProps,
                }}
                className={this.props.className}
                confirmCallback={this.onDelete}
                confirmTitle="Are you sure?"
                confirmMessage={`Are you sure you want to remove dashboard ${this.props.dashboard ? this.props.dashboard.name : ''}?`}
            >
                {this.props.children}
            </ConfirmButton>
        )
    }
}

export const mapStateToProps = (state: { dashboard: DashboardState }): StateProps => parseDashboard(state)

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    deleteDashboard(id: $ElementType<Dashboard, 'id'>) {
        return dispatch(deleteDashboard(id))
    },
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardDeleteButton)

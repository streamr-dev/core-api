// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import ConfirmButton from '../../ConfirmButton'
import createLink from '../../../helpers/createLink'

import {deleteDashboard} from '../../../actions/dashboard'
import {parseDashboard} from '../../../helpers/parseState'

import type {ReactChildren} from 'react-flow-types'
import type {Dashboard} from '../../../flowtype/dashboard-types'

class DeleteButton extends Component {
    onDelete: Function
    props: {
        dashboard: Dashboard,
        canWrite: boolean,
        buttonProps: {},
        children?: ReactChildren,
        deleteDashboard: () => Promise<any>
    }
    static defaultProps = {
        buttonProps: {}
    }
    
    constructor() {
        super()
        
        this.onDelete = this.onDelete.bind(this)
    }
    
    onDelete() {
        this.props.deleteDashboard(this.props.dashboard.id)
            .then(() => {
                window.location = createLink('/dashboard/list')
            })
    }
    
    render() {
        return (
            <ConfirmButton
                buttonProps={{
                    disabled: !this.props.canWrite,
                    ...this.props.buttonProps
                }}
                confirmCallback={this.onDelete}
                confirmTitle="Are you sure?"
                confirmMessage={`Are you sure you want to remove dashboard ${this.props.dashboard.name}?`}
            >
                {this.props.children}
            </ConfirmButton>
        )
    }
}

const mapStateToProps = (state) => parseDashboard(state)

const mapDispatchToProps = (dispatch) => ({
    deleteDashboard(id: Dashboard.id) {
        return dispatch(deleteDashboard(id))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DeleteButton)
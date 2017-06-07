// @flow

import React, {Component} from 'react'
import {any} from 'prop-types'
import {connect} from 'react-redux'
import {Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import ShareDialog from '../../../ShareDialog'

import {updateAndSaveDashboard, deleteDashboard} from '../../../../actions/dashboard'

import type { Dashboard } from '../../../../flowtype/dashboard-types'

declare var ConfirmButton: Function
declare var Streamr: any

class DashboardTools extends Component {
    
    removeButton: HTMLButtonElement
    onSave: Function
    onDelete: Function
    
    props: {
        dashboard: Dashboard,
        openDashboard: {
            new: boolean
        },
        dispatch: Function,
        router: any
    }
    
    static contextTypes = {
        router: any
    }
    
    constructor() {
        super()
        
        this.onSave = this.onSave.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }
    
    componentDidMount() {
        // TODO: message
        new ConfirmButton(this.removeButton, {
            title: 'Are you sure?',
            message: `Are you sure you want to remove dashboard ${this.props.dashboard ? this.props.dashboard.name : ''}?`
        }, res => {
            if (res) {
                this.onDelete()
            }
        })
    }
    
    onSave() {
        this.props.dispatch(updateAndSaveDashboard(this.props.dashboard, this.props.openDashboard.new || false))
            .then(({dashboard}) => {
                this.context.router.push(`/${dashboard.id}`)
            })
    }
    
    onDelete() {
        this.props.dispatch(deleteDashboard(this.props.dashboard.id))
            .then(() => window.location = Streamr.createLink({
                uri: '/dashboards/list'
            }))
    }
    
    render() {
        return (
            <div className="menu-content">
                <Button
                    block
                    className="save-button"
                    title="Save dashboard"
                    bsStyle="primary"
                    onClick={this.onSave}
                >
                    Save
                </Button>
                <ShareDialog>
                    <Button
                        block
                        className="share-button"
                    >
                        <FontAwesome name="user" />  Share
                    </Button>
                </ShareDialog>
                <button
                    className="btn btn-default btn-block delete-button"
                    title="Delete dashboard"
                    ref={item => this.removeButton = item}
                >
                    Delete
                </button>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => ({
    openDashboard: dashboard.openDashboard || {}
})

export default connect(mapStateToProps)(DashboardTools)
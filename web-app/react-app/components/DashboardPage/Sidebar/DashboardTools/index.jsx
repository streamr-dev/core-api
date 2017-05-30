// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import path from 'path'

import {updateAndSaveDashboard, deleteDashboard} from '../../../../actions/dashboard'

import type { Dashboard } from '../../../../types'

declare var ConfirmButton: Function
declare var Streamr: any

class DashboardTools extends Component {
    
    removeButton: HTMLButtonElement
    onSave: Function
    onShare: Function
    onDelete: Function
    
    props: {
        dashboard: Dashboard,
        dispatch: Function
    }
    
    constructor() {
        super()
        
        this.onSave = this.onSave.bind(this)
        this.onShare = this.onShare.bind(this)
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
        this.props.dispatch(updateAndSaveDashboard(this.props.dashboard))
            .then(() => Streamr.showSuccess('Dashboard saved!'))
    }
    
    onShare() {
    
    }
    
    onDelete() {
        this.props.dispatch(deleteDashboard(this.props.dashboard.id))
            .then(() => window.location = path.resolve(window.location.href, '..'))
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
                <Button
                    block
                    className="share-button"
                    onClick={this.onShare}
                >
                    <FontAwesome name="user" />  Share
                </Button>
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

export default connect()(DashboardTools)
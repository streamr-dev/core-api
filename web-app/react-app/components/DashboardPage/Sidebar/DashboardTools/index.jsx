// @flow

import React, {Component} from 'react'
import {any} from 'prop-types'
import {connect} from 'react-redux'
import {Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import ConfirmButton from '../../../ConfirmButton'
import ShareDialog from '../../../ShareDialog'

import {updateAndSaveDashboard, deleteDashboard} from '../../../../actions/dashboard'

import type { Dashboard } from '../../../../flowtype/dashboard-types'

declare var Streamr: any

class DashboardTools extends Component {
    
    onSave: Function
    onDelete: Function
    props: {
        dashboard: Dashboard,
        openDashboard: {
            new: boolean
        },
        dispatch: Function,
        router: any,
        canShare: boolean,
        canWrite: boolean
    }
    
    static contextTypes = {
        router: any
    }
    
    constructor() {
        super()
        
        this.onSave = this.onSave.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }

    onSave() {
        this.props.dispatch(updateAndSaveDashboard(this.props.dashboard))
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
                <ShareDialog
                    resourceType="DASHBOARD"
                    resourceId={this.props.dashboard.id}
                    resourceTitle={`Dashboard ${this.props.dashboard.name}`}
                >
                    <Button
                        block
                        className="share-button"
                        disabled={!this.props.canShare}
                    >
                        <FontAwesome name="user" />  Share
                    </Button>
                </ShareDialog>
                <ConfirmButton
                    buttonProps={{
                        block: true,
                        disabled: !this.props.canWrite
                    }}
                    confirmCallback={this.onDelete}
                    confirmTitle="Are you sure?"
                    confirmMessage={`Are you sure you want to remove dashboard ${this.props.dashboard.name}?`}
                >
                    Delete
                </ConfirmButton>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => {
    const db = dashboard.dashboardsById[dashboard.openDashboard.id] || {}
    return {
        dashboard: db,
        canShare: db.new !== true && (db.ownPermissions && db.ownPermissions.includes('share')),
        canWrite: db.new !== true && (db.ownPermissions && db.ownPermissions.includes('write'))
    }
}

export default connect(mapStateToProps)(DashboardTools)
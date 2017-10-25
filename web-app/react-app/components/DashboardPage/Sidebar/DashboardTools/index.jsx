// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import {parseDashboard} from '../../../../helpers/parseState'

import DeleteButton from '../../DashboardDeleteButton'
import ShareDialog from '../../../ShareDialog'

import {updateAndSaveDashboard} from '../../../../actions/dashboard'

import type { Dashboard } from '../../../../flowtype/dashboard-types'

class DashboardTools extends Component {
    
    onSave: Function
    props: {
        dashboard: Dashboard,
        openDashboard: {
            new: boolean
        },
        dispatch: Function,
        router: any,
        canShare: boolean,
        canWrite: boolean,
        updateAndSaveDashboard: Function
    }
    
    constructor() {
        super()
        
        this.onSave = this.onSave.bind(this)
    }

    onSave() {
        this.props.updateAndSaveDashboard(this.props.dashboard)
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
                    disabled={!this.props.canWrite && !this.props.dashboard.new}
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
                <DeleteButton buttonProps={{
                    block: true
                }}>
                    Delete
                </DeleteButton>
            </div>
        )
    }
}

const mapStateToProps = (state) => parseDashboard(state)

const mapDispatchToProps = (dispatch) => ({
    updateAndSaveDashboard(db) {
        return dispatch(updateAndSaveDashboard(db))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardTools)
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

type Props = {
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

type State = {
    shareDialogIsOpen: boolean
}

class DashboardTools extends Component<Props, State> {

    state = {
        shareDialogIsOpen: false
    }
    
    onSave = () => {
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
                <Button
                    block
                    className="share-button"
                    disabled={!this.props.canShare}
                    onClick={() => {
                        this.setState({
                            shareDialogIsOpen: true
                        })
                    }}
                >
                    <FontAwesome name="user" />  Share
                </Button>
                <ShareDialog
                    resourceType="DASHBOARD"
                    resourceId={this.props.dashboard.id}
                    resourceTitle={`Dashboard ${this.props.dashboard.name}`}
                    isOpen={this.state.shareDialogIsOpen}
                    onClose={() => {
                        this.setState({
                            shareDialogIsOpen: false
                        })
                    }}
                />
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
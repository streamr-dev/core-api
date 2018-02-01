// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import {parseDashboard} from '../../../../helpers/parseState'

import DeleteButton from '../../DashboardDeleteButton'
import ShareDialog from '../../../ShareDialog'

import {updateAndSaveDashboard} from '../../../../actions/dashboard'

import type { DashboardState } from '../../../../flowtype/states/dashboard-state'
import type { Dashboard } from '../../../../flowtype/dashboard-types'

import styles from './dashboardTools.pcss'

type StateProps = {
    dashboard: ?Dashboard,
    canShare: boolean,
    canWrite: boolean
}

type DispatchProps = {
    updateAndSaveDashboard: (db: Dashboard) => void
}

type Props = StateProps & DispatchProps

type State = {
    shareDialogIsOpen: boolean
}

export class DashboardTools extends Component<Props, State> {

    state = {
        shareDialogIsOpen: false
    }
    
    onSave = () => {
        this.props.dashboard && this.props.updateAndSaveDashboard(this.props.dashboard)
    }
    
    render() {
        return (
            <div className={`menu-content ${styles.dashboardTools}`}>
                <Button
                    block
                    className={styles.saveButton}
                    title="Save dashboard"
                    bsStyle="primary"
                    onClick={this.onSave}
                    disabled={!this.props.canWrite && (!this.props.dashboard || !this.props.dashboard.new)}
                >
                    <FontAwesome name="floppy-o" />  Save
                </Button>
                <Button
                    block
                    className={styles.shareButton}
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
                    resourceId={this.props.dashboard && this.props.dashboard.id}
                    resourceTitle={`Dashboard ${this.props.dashboard ? this.props.dashboard.name : ''}`}
                    isOpen={this.state.shareDialogIsOpen}
                    onClose={() => {
                        this.setState({
                            shareDialogIsOpen: false
                        })
                    }}
                />
                <DeleteButton
                    className={styles.deleteButton}
                    buttonProps={{
                        block: true
                    }}
                >
                    <FontAwesome name="trash-o" />  Delete
                </DeleteButton>
            </div>
        )
    }
}

export const mapStateToProps = (state: {dashboard: DashboardState}): StateProps => parseDashboard(state)

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    updateAndSaveDashboard(db: Dashboard) {
        return dispatch(updateAndSaveDashboard(db))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardTools)

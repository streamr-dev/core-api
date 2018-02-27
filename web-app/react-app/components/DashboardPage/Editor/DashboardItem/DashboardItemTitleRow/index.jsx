// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'
import {Button} from 'react-bootstrap'

import {removeDashboardItem, updateDashboardItem} from '../../../../../actions/dashboard'

import styles from './dashboardItemTitleRow.pcss'

import type {DashboardState} from '../../../../../flowtype/states/dashboard-state'
import type {Dashboard, DashboardItem} from '../../../../../flowtype/dashboard-types'

type StateProps = {
    dashboard: ?Dashboard
}

type DispatchProps = {
    update: Function,
    remove: Function
}

type GivenProps = {
    item: DashboardItem,
    className?: string,
    dragCancelClassName?: string,
    isLocked: boolean
}

type Props = StateProps & DispatchProps & GivenProps

type State = {
    editing: boolean
}

export class DashboardItemTitleRow extends Component<Props, State> {
    saveButton: ?HTMLElement
    static defaultProps = {
        isLocked: false,
    }

    state = {
        editing: false,
    }

    onRemove = () => {
        this.props.remove(this.props.dashboard, this.props.item)
    }

    startEdit = () => {
        this.setState({
            editing: true,
        })
    }

    onBlur = (e: { relatedTarget: HTMLElement }) => {
        // This hack prevents clicking saveButton from first closing the editing and the starting it again
        if (this.saveButton && this.saveButton !== e.relatedTarget && !this.saveButton.contains(e.relatedTarget)) {
            this.endEdit()
        }
    }

    endEdit = () => {
        this.setState({
            editing: false,
        })
    }

    saveName = ({target}: { target: { value: string } }) => {
        this.props.update(this.props.dashboard, this.props.item, {
            title: target.value,
        })
    }

    render() {
        const {item, dragCancelClassName} = this.props
        return (
            <div className={styles.titleRow}>
                <div className={styles.title}>
                    {this.state.editing ? (
                        <input
                            className={`titlebar-edit name-input form-control input-sm ${dragCancelClassName || ''}`}
                            type="text"
                            placeholder="Title"
                            name="dashboard-item-name"
                            value={item.title}
                            onChange={this.saveName}
                            onBlur={this.onBlur}
                        />
                    ) : (
                        <span className={dragCancelClassName}>
                            {item.title}
                        </span>
                    )}
                </div>
                {!this.props.isLocked && (
                    <div className={styles.controlContainer}>
                        <div className={`${styles.controls} ${dragCancelClassName || ''}`}>
                            {this.state.editing ? (
                                <Button
                                    bsSize="xs"
                                    bsStyle="default"
                                    className={`btn-outline dark ${styles.endEditButton}`}
                                    title="Ready"
                                    onClick={this.endEdit}
                                    componentClass={(props) => <button {...props} ref={el => this.saveButton = el}/>}
                                >
                                    <FontAwesome name="check"/>
                                </Button>
                            ) : (
                                <Button
                                    bsSize="xs"
                                    bsStyle="default"
                                    className={`btn-outline dark ${styles.startEditButton}`}
                                    title="Edit title"
                                    onClick={this.startEdit}
                                >
                                    <FontAwesome name="edit"/>
                                </Button>
                            )}

                            <Button
                                bsSize="xs"
                                bsStyle="default"
                                className={`btn-outline dark ${styles.deleteButton}`}
                                title="Remove"
                                onClick={this.onRemove}
                            >
                                <FontAwesome name="times"/>
                            </Button>
                        </div>
                    </div>
                )}
            </div>
        )
    }
}

export const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}: { dashboard: DashboardState }): StateProps => ({
    dashboard: openDashboard.id ? dashboardsById[openDashboard.id] : null,
})

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    update(db: Dashboard, item: DashboardItem, newData: {} = {}) {
        return dispatch(updateDashboardItem(db, {
            ...item,
            ...newData,
        }))
    },
    remove(db: Dashboard, item: DashboardItem) {
        return dispatch(removeDashboardItem(db, item))
    },
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItemTitleRow)

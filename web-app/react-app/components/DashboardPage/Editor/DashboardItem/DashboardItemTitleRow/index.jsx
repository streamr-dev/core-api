// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'
import {Button} from 'react-bootstrap'

import {removeDashboardItem, updateDashboardItem} from '../../../../../actions/dashboard'

import styles from './dashboardItemTitleRow.pcss'

import type {Dashboard, DashboardItem, DashboardReducerState as DashboardState} from '../../../../../flowtype/dashboard-types'

type Props = {
    item: DashboardItem,
    dashboard: Dashboard,
    update: Function,
    remove: Function,
    className?: string,
    dragCancelClassName?: string,
    isLocked: boolean
}

type State = {
    editing: boolean
}

export class DashboardItemTitleRow extends Component<Props, State> {
    
    static defaultProps = {
        isLocked: false
    }
    
    state = {
        editing: false
    }
    
    onRemove = () => {
        this.props.remove(this.props.dashboard, this.props.item)
    }
    
    startEdit = () => {
        this.setState({
            editing: true
        })
    }
    
    endEdit = () => {
        this.setState({
            editing: false
        })
    }
    
    saveName = ({target}: {target: {value: string}}) => {
        this.props.update(this.props.dashboard, this.props.item, {
            title: target.value
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
                            onBlur={this.endEdit}
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

export const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}: {dashboard: DashboardState}) => ({
    dashboard: dashboardsById[openDashboard.id]
})

export const mapDispatchToProps = (dispatch: Function) => ({
    update(db: Dashboard, item: DashboardItem, newData: {} = {}) {
        return dispatch(updateDashboardItem(db, {
            ...item,
            ...newData
        }))
    },
    remove(db: Dashboard, item: DashboardItem) {
        return dispatch(removeDashboardItem(db, item))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItemTitleRow)
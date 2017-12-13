// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'
import {Button} from 'react-bootstrap'

import {removeDashboardItem, updateDashboardItem} from '../../../../../actions/dashboard'

import styles from './dashboardItemTitleRow.pcss'

import type {Dashboard, DashboardItem} from '../../../../../flowtype/dashboard-types'

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

class DashboardItemTitleRow extends Component<Props, State> {
    
    static defaultProps = {
        editingLocked: false
    }
    
    state = {
        editing: false
    }
    
    onRemove = () => {
        this.props.remove(this.props.dashboard, this.props.item)
    }
    
    toggleEdit = () => {
        this.setState({
            editing: !this.state.editing
        })
    }
    
    saveName = ({target}) => {
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
                            onBlur={this.toggleEdit}
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
                            <Button
                                bsSize="xs"
                                bsStyle="default"
                                className="btn-outline dark"
                                title={this.state.editing ? 'Ready' : 'Edit title'}
                                onClick={this.toggleEdit}
                            >
                                <FontAwesome name={this.state.editing ? 'check' : 'edit'}/>
                            </Button>
                            <button
                                className="delete-btn btn btn-xs btn-outline dark"
                                title="Remove"
                                onClick={this.onRemove}
                            >
                                <i className="fa fa-times"/>
                            </button>
                        </div>
                    </div>
                )}
            </div>
        )
    }
}

const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}) => ({
    dashboard: dashboardsById[openDashboard.id]
})

const mapDispatchToProps = (dispatch) => ({
    update(db: Dashboard, item: DashboardItem, newData) {
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
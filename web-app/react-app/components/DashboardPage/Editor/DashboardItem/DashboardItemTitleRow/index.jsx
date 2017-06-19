// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import FontAwesome from 'react-fontawesome'
import {Button} from 'react-bootstrap'

import {removeDashboardItem, updateDashboardItem} from '../../../../../actions/dashboard'

import styles from './dashboardItemTitleRow.pcss'

import type {Dashboard, DashboardItem} from '../../../../../flowtype/dashboard-types'

class DashboardItemTitleRow extends Component {
    onRemove: Function
    toggleEdit: Function
    saveName: Function
    props: {
        item: DashboardItem,
        update: Function,
        remove: Function,
        dashboard: Dashboard,
        className?: string,
        dragCancelClassName?: string,
        editingLocked: boolean
    }
    state: {
        editing: boolean
    }
    static defaultProps = {
        editingLocked: false
    }
    
    constructor() {
        super()
        this.state = {
            editing: false
        }
        this.onRemove = this.onRemove.bind(this)
        this.toggleEdit = this.toggleEdit.bind(this)
        this.saveName = this.saveName.bind(this)
    }
    
    onRemove() {
        this.props.remove()
    }
    
    toggleEdit() {
        this.setState({
            editing: !this.state.editing
        })
    }
    
    saveName({target}) {
        this.props.update({
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
                            value={this.props.item.title}
                            onChange={this.saveName}
                            onBlur={this.toggleEdit}
                        />
                    ) : (
                        <span className={dragCancelClassName}>
                            {item.title}
                        </span>
                    )}
                </div>
                {!this.props.editingLocked && (
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

const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}) => {
    const dashboard = dashboardsById[openDashboard.id]
    return {
        dashboard,
        editingLocked: dashboard.editingLocked || (!dashboard.new && (!(dashboard.ownPermissions || []).includes('write')))
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    update(newData) {
        return dispatch(updateDashboardItem(ownProps.dashboard, {
            ...ownProps.item,
            ...newData
        }))
    },
    remove() {
        return dispatch(removeDashboardItem(ownProps.dashboard, ownProps.item))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItemTitleRow)
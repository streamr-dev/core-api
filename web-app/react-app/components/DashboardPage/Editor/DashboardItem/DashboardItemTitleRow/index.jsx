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
        dashboard: Dashboard,
        dispatch: Function,
        className?: string,
        dragCancelClassName?: string
    }
    state: {
        editing: boolean
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
        this.props.dispatch(removeDashboardItem(this.props.dashboard, this.props.item))
    }
    
    toggleEdit() {
        this.setState({
            editing: !this.state.editing
        })
    }
    
    saveName({target}) {
        this.props.dispatch(updateDashboardItem(this.props.dashboard, {
            ...this.props.item,
            title: target.value
        }))
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
                        <span>
                            {item.title}
                        </span>
                    )}
                </div>
                <div className={styles.controlContainer}>
                    <div className={styles.controls}>
                        <Button
                            bsSize="xs"
                            bsStyle="default"
                            className={`btn-outline dark ${dragCancelClassName || ''}`}
                            title={this.state.editing ? 'Ready' : 'Edit title'}
                            onClick={this.toggleEdit}
                        >
                            <FontAwesome name={this.state.editing ? 'check' : 'edit'}/>
                        </Button>
                        <button
                            className={`delete-btn btn btn-xs btn-outline dark ${dragCancelClassName || ''}`}
                            title="Remove"
                            onClick={this.onRemove}
                        >
                            <i className="fa fa-times"/>
                        </button>
                    </div>
                </div>
            </div>
        )
    }
}

export default connect()(DashboardItemTitleRow)
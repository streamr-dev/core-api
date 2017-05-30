// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {removeDashboardItem} from '../../../../../actions/dashboard'

import type {Dashboard, DashboardItem} from '../../../../../types/dashboard-types'

class DashboardItemTitleRow extends Component {
    onRemove: Function
    props: {
        item: DashboardItem,
        dashboard: Dashboard,
        dispatch: Function
    }
    
    constructor() {
        super()
        this.onRemove = this.onRemove.bind(this)
    }
    
    onRemove() {
        this.props.dispatch(removeDashboardItem(this.props.dashboard, this.props.item))
    }
    
    render() {
        const {item} = this.props
        return (
            <div className="title stat-cell bg-dark-gray padding-sm text-s text-semibold">
                <div className="col-xs-7">
                    <span className="titlebar">
                        {item.title}
                    </span>
                    {/*<input className="titlebar-edit name-input form-control input-sm" type="text" value="Button" placeholder="Title" name="dashboard-item-name"/>*/}
                </div>
                <div className="panel-heading-controls text-left">
                    <button className="edit-btn btn btn-xs btn-outline dark" title="Edit title"><i className="fa fa-edit"/></button>
                    <button className="close-edit btn btn-xs btn-outline dark" title="Ready"><i className="fa fa-check"/></button>
                    <div className="btn-group btn-group-xs">
                        <button data-toggle="dropdown" type="button" className="btn btn-outline dark dropdown-toggle" title="Edit size">
                            <span className="fa fa-expand"/>
                            &nbsp;
                            <span className="fa fa-caret-down"/>
                        </button>
                        <ul className="dropdown-menu pull-right">
                            <li className="checked">
                                <a href="#" className="make-small-btn">
                                    <i className="fa fa-check"/> Small
                                </a>
                            </li>
                            <li>
                                <a href="#" className="make-medium-btn">
                                    <i className="fa fa-check"/> Medium
                                </a>
                            </li>
                            <li>
                                <a href="#" className="make-large-btn">
                                    <i className="fa fa-check"/> Large
                                </a>
                            </li>
                        </ul>
                    </div>
                    <button
                        className="delete-btn btn btn-xs btn-outline dark"
                        title="Remove"
                        onClick={this.onRemove}
                    >
                        <i className="fa fa-times"/>
                    </button>
                </div>
            </div>
        )
    }
}

export default connect()(DashboardItemTitleRow)
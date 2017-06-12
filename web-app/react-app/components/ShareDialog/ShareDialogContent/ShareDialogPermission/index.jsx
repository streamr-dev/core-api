// @flow

import React, {Component} from 'react'
import {Col, DropdownButton, MenuItem} from 'react-bootstrap'

import styles from './shareDialogPermission.pcss'

import type {Permission} from '../../../../flowtype/permission-types'

export default class ShareDialogPermission extends Component {
    props: {
        permission: Permission
    }
    
    render() {
        const {permission} = this.props
        return (
            <Col xs={12} className={styles.permissionRow}>
                <span className="user-label col-xs-6">
                    {permission.user}
                </span>
                <button className="form-group user-delete-button btn btn-danger pull-right">
                    <span className="icon fa fa-trash-o"/>
                </button>
                <button type="button" className="btn btn-default dropdown-toggle permission-dropdown-toggle pull-right"
                        data-toggle="dropdown">
                    <span className="state">can read</span>
                    <span className="caret"/>
                </button>
                <ul className="dropdown-menu">
                    <li data-opt="read"><a href="#">make read-only</a></li>
                    <li data-opt="write"><a href="#">make editable</a></li>
                    <li data-opt="share"><a href="#">make shareable</a></li>
                </ul>
                <DropdownButton>
                    <MenuItem eventKey="1">make read-only</MenuItem>
                    <MenuItem eventKey="2">make editable</MenuItem>
                </DropdownButton>
            </Col>
        )
    }
}
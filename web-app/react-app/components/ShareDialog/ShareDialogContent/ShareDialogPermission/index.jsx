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
                <DropdownButton title="moi" id={`shareDialogPermissionInputDropdown-${Date.now()}`}>
                    <MenuItem eventKey="1" id="1">make read-only</MenuItem>
                    <MenuItem eventKey="2" id="2">make editable</MenuItem>
                    <MenuItem eventKey="3" id="3">make shareable</MenuItem>
                </DropdownButton>
            </Col>
        )
    }
}
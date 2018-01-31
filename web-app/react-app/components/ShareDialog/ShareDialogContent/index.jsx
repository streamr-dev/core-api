// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {Row, Modal} from 'react-bootstrap'

import ShareDialogInputRow from './ShareDialogInputRow'
import ShareDialogPermissionRow from './ShareDialogPermissionRow'
import ShareDialogAnonymousAccessRow from './ShareDialogOwnerRow'

import type {Permission} from '../../../flowtype/permission-types'
import {getResourcePermissions} from '../../../actions/permission'

type Props = {
    permissions: Array<Permission>,
    resourceType: Permission.resourceType,
    resourceId: Permission.resourceId,
    anonymousPermission: ?Permission,
    owner: ?string,
    getResourcePermissions: () => {},
    addPermission: (permission: Permission) => {},
    removePermission: (permission: Permission) => {},
    onClose: () => {}
}

export class ShareDialogContent extends Component<Props> {
    
    componentWillMount() {
        this.props.getResourcePermissions()
    }
    
    render() {
        return (
            <Modal.Body>
                <Row>
                    <ShareDialogAnonymousAccessRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                    />
                    <ShareDialogPermissionRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                    />
                    <ShareDialogInputRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                        onClose={this.props.onClose}
                    />
                </Row>
            </Modal.Body>
        )
    }
}

export const mapDispatchToProps = (dispatch: Function, ownProps: Props) => ({
    getResourcePermissions() {
        dispatch(getResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect(null, mapDispatchToProps)(ShareDialogContent)
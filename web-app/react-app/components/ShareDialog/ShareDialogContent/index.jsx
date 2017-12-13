// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {Row, Modal} from 'react-bootstrap'

import ShareDialogInputRow from './ShareDialogInputRow'
import ShareDialogPermissionRow from './ShareDialogPermissionRow'
import ShareDialogOwnerRow from './ShareDialogOwnerRow'

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
    removePermission: (permission: Permission) => {}
}

class ShareDialogContent extends Component<Props> {
    form: ?HTMLFormElement
    
    componentWillMount() {
        this.props.getResourcePermissions()
    }
    
    onAnonymousAccessChange = () => {
        const permission = this.props.anonymousPermission || {
            anonymous: true,
            operation: 'read'
        }
        if (this.props.anonymousPermission) {
            this.props.removePermission(permission)
        } else {
            this.props.addPermission(permission)
        }
    }
    onSubmit = (e) => {
        e.preventDefault()
        const data = serialize(this.form, {
            hash: true
        })
        this.props.addPermission({
            user: data.email,
            operation: 'read'
        })
        this.form && this.form.reset()
    }
    render() {
        return (
            <Modal.Body>
                <Row>
                    <ShareDialogOwnerRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                    />
                    <Row>
                        <ShareDialogPermissionRow
                            resourceType={this.props.resourceType}
                            resourceId={this.props.resourceId}
                        />
                    </Row>
                    <ShareDialogInputRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                    />
                </Row>
            </Modal.Body>
        )
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    getResourcePermissions() {
        dispatch(getResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect(null, mapDispatchToProps)(ShareDialogContent)
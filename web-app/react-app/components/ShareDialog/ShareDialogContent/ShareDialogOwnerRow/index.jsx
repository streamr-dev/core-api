// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Col} from 'react-bootstrap'
import Switcher from 'react-switcher'

import type {PermissionState} from '../../../../flowtype/states/permission-state'
import type {Permission, ResourceType, ResourceId} from '../../../../flowtype/permission-types'
import {addResourcePermission, removeResourcePermission} from '../../../../actions/permission'

import styles from './shareDialogOwnerRow.pcss'

type StateProps = {
    anonymousPermission: ?Permission,
    owner: ?string
}

type DispatchProps = {
    addPublicPermission: () => void,
    revokePublicPermission: (permission: Permission) => void
}

type GivenProps = {
    resourceType: ResourceType,
    resourceId: ResourceId
}

type Props = StateProps & DispatchProps & GivenProps

export class ShareDialogOwnerRow extends Component<Props> {
    
    onAnonymousAccessChange = () => {
        if (!this.props.anonymousPermission) {
            this.props.addPublicPermission()
        } else {
            this.props.revokePublicPermission(this.props.anonymousPermission)
        }
    }
    
    render() {
        return (
            <Col xs={12} className={styles.ownerRow}>
                <div className={styles.readAccessLabel}>
                    Public read access
                </div>
                <div className={styles.readAccess}>
                    <Switcher on={this.props.anonymousPermission !== undefined} onClick={this.onAnonymousAccessChange}/>
                </div>
            </Col>
        )
    }
}

export const mapStateToProps = ({permission: {byTypeAndId}}: {permission: PermissionState}, ownProps: Props): StateProps => {
    const byType = byTypeAndId[ownProps.resourceType] || {}
    const permissions = (byType[ownProps.resourceId] || []).filter(p => !p.removed)
    const ownerPermission = permissions.find(it => it.id === null && !it.new) || {}
    const owner = ownerPermission.user
    return {
        anonymousPermission: permissions.find(p => p.anonymous),
        owner
    }
}

export const mapDispatchToProps = (dispatch: Function, ownProps: Props): DispatchProps => ({
    addPublicPermission() {
        dispatch(addResourcePermission(ownProps.resourceType, ownProps.resourceId, {
            user: null,
            anonymous: true,
            operation: 'read'
        }))
    },
    revokePublicPermission(anonymousPermission: Permission) {
        dispatch(removeResourcePermission(ownProps.resourceType, ownProps.resourceId, anonymousPermission))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ShareDialogOwnerRow)
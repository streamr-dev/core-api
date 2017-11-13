// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Col} from 'react-bootstrap'
import Switcher from 'react-switcher'

import type {Permission} from '../../../../flowtype/permission-types'
import {addResourcePermission, removeResourcePermission} from '../../../../actions/permission'

import styles from './shareDialogOwnerRow.pcss'

export class ShareDialogOwnerRow extends Component {
    onAnonymousAccessChange: Function
    props: {
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        anonymousPermission: ?Permission,
        owner: ?string,
        addPublicPermission: (permission: Permission) => {},
        revokePublicPermission: (permission: Permission) => {}
    }
    
    constructor() {
        super()
        
        this.onAnonymousAccessChange = this.onAnonymousAccessChange.bind(this)
    }
    
    onAnonymousAccessChange() {
        if (!this.props.anonymousPermission) {
            this.props.addPublicPermission()
        } else {
            this.props.revokePublicPermission()
        }
    }
    
    render() {
        return (
            <Col xs={12} className={styles.ownerRow}>
                <div className={styles.ownerLabel}>
                    Owner:
                </div>
                <div className={styles.owner}>
                    <strong>{this.props.owner}</strong>
                </div>
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

const mapStateToProps = ({permission: {byTypeAndId}}, ownProps) => {
    const byType = byTypeAndId[ownProps.resourceType] || {}
    const permissions = (byType[ownProps.resourceId] || []).filter(p => !p.removed)
    const ownerPermission = permissions.find(it => it.id === null && !it.new) || {}
    const owner = ownerPermission.user
    return {
        anonymousPermission: permissions.find(p => p.anonymous),
        owner
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    addPublicPermission() {
        dispatch(addResourcePermission(ownProps.resourceType, ownProps.resourceId, {
            anonymous: true,
            operation: 'read'
        }))
    },
    revokePublicPermission() {
        dispatch(removeResourcePermission(ownProps.resourceType, ownProps.resourceId, ownProps.anonymousPermission))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ShareDialogOwnerRow)
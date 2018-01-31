// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import _ from 'lodash'

import ShareDialogPermission from './ShareDialogPermission'

import styles from './shareDialogPermissionRow.pcss'
import type {Permission, State as PermissionState} from '../../../../flowtype/permission-types'
import {Row, Col} from 'react-bootstrap'

type Props = {
    permissions: Array<Permission>,
    resourceType: Permission.resourceType,
    resourceId: Permission.resourceId
}

export class ShareDialogPermissionRow extends Component<Props> {
    
    render() {
        return (
            <Row>
                <Col xs={12} className={styles.permissionRow}>
                    {_.chain(this.props.permissions)
                        .groupBy(p => p.user) // Arrays of permissions with users as keys
                        .mapValues(permissions => (
                            <ShareDialogPermission
                                resourceType={this.props.resourceType}
                                resourceId={this.props.resourceId}
                                key={`${permissions[0].user}`}
                                permissions={permissions}
                            />
                        ))
                        .values() // Take only the components
                        .value() // Output the array
                    }
                </Col>
            </Row>
        )
    }
}

export const mapStateToProps = ({permission: {byTypeAndId}}: {permission: PermissionState}, ownProps: Props) => {
    const byType = byTypeAndId[ownProps.resourceType] || {}
    const permissions = (byType[ownProps.resourceId] || []).filter(p => !p.removed)
    return {
        permissions: permissions.filter(p => !p.anonymous && (p.id || p.new))
    }
}

export default connect(mapStateToProps)(ShareDialogPermissionRow)
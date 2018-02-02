// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button, Col} from 'react-bootstrap'
import Select from 'react-select'
import FontAwesome from 'react-fontawesome'

import {setResourceHighestOperationForUser, removeAllResourcePermissionsByUser} from '../../../../../actions/permission'

import 'react-select/dist/react-select.css'
import styles from './shareDialogPermission.pcss'

import type {Permission, ResourceType, ResourceId} from '../../../../../flowtype/permission-types'

declare var Streamr: any

type StateProps = {}

type DispatchProps = {
    setResourceHighestOperation: (value: $ElementType<Permission, 'operation'>) => void,
    remove: () => void
}

type GivenProps = {
    resourceType: ResourceType,
    resourceId: ResourceId,
    permissions: Array<Permission>
}

type Props = StateProps & DispatchProps & GivenProps

const operationsInOrder = ['read', 'write', 'share']

export class ShareDialogPermission extends Component<Props> {
    
    onSelect = ({value}: {value: $ElementType<Permission, 'operation'>}) => {
        this.props.setResourceHighestOperation(value)
    }
    
    onRemove = () => {
        this.props.remove()
    }
    
    render() {
        const errors = this.props.permissions.filter(p => p.error).map(p => p.error && p.error.error)
        const highestOperationIndex = Math.max(...(this.props.permissions.map(p => operationsInOrder.indexOf(p.operation))))
        const user = this.props.permissions[0] && this.props.permissions[0].user
        return (
            <Col xs={12} className={styles.permissionRow}>
                {errors.length ? (
                    <div className={styles.errorContainer} title={errors.join('\n')}>
                        <FontAwesome name="exclamation-circle" className="text-danger"/>
                    </div>
                ) : null}
                {user === Streamr.user ? (
                    <span className={styles.userLabel}>
                        <strong className={styles.meLabel}>Me</strong>
                        <span>({user})</span>
                    </span>
                ) : (
                    <span className={styles.userLabel}>
                        {user}
                    </span>
                )}
                <Select
                    className={styles.select}
                    value={operationsInOrder[highestOperationIndex]}
                    options={operationsInOrder.map(o => ({
                        value: o,
                        label: `can ${o}`
                    }))}
                    clearable={false}
                    searchable={false}
                    autosize={false}
                    onChange={this.onSelect}
                />
                <Button bsStyle="danger" onClick={this.onRemove}>
                    <FontAwesome name="trash-o"/>
                </Button>
            </Col>
        )
    }
}

export const mapDispatchToProps = (dispatch: Function, ownProps: GivenProps): DispatchProps => ({
    setResourceHighestOperation(value: $ElementType<Permission, 'operation'>) {
        const user = ownProps.permissions[0] && ownProps.permissions[0].user
        user && dispatch(setResourceHighestOperationForUser(ownProps.resourceType, ownProps.resourceId, user, value))
    },
    remove() {
        const user = ownProps.permissions[0] && ownProps.permissions[0].user
        user && dispatch(removeAllResourcePermissionsByUser(ownProps.resourceType, ownProps.resourceId, user))
    }
})

export default connect(null, mapDispatchToProps)(ShareDialogPermission)

// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button, Col} from 'react-bootstrap'
import Select from 'react-select'
import FontAwesome from 'react-fontawesome'

import {setResourceHighestOperationForUser, removeAllResourcePermissionsByUser} from '../../../../../actions/permission'

import 'react-select/dist/react-select.css'
import styles from './shareDialogPermission.pcss'

import type {Permission} from '../../../../../flowtype/permission-types'

const operationsInOrder = ['read', 'write', 'share']

class ShareDialogPermission extends Component {
    onSelect: Function
    onRemove: Function
    props: {
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        permissions: Array<Permission>,
        setResourceHighestOperation: () => {},
        remove: () => {}
    }
    
    constructor() {
        super()
        this.onSelect = this.onSelect.bind(this)
        this.onRemove = this.onRemove.bind(this)
    }
    
    onSelect({value}) {
        this.props.setResourceHighestOperation(value)
    }
    
    onRemove() {
        this.props.remove()
    }
    
    render() {
        const errors = this.props.permissions.filter(p => p.error).map(p => p.error.message || p.error.error)
        const highestOperationIndex = Math.max(...(this.props.permissions.map(p => operationsInOrder.indexOf(p.operation))))
        return (
            <Col xs={12} className={styles.permissionRow}>
                {errors.length ? (
                    <div className={styles.errorContainer} title={errors.join('\n')}>
                        <FontAwesome name="exclamation-circle" className="text-danger"/>
                    </div>
                ) : null}
                <span className={styles.userLabel}>
                    {this.props.permissions[0].user}
                </span>
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

const mapDispatchToProps = (dispatch, ownProps) => ({
    setResourceHighestOperation(value) {
        dispatch(setResourceHighestOperationForUser(ownProps.resourceType, ownProps.resourceId, ownProps.permissions[0].user, value))
    },
    remove() {
        dispatch(removeAllResourcePermissionsByUser(ownProps.resourceType, ownProps.resourceId, ownProps.permissions[0].user))
    }
})

export default connect(null, mapDispatchToProps)(ShareDialogPermission)
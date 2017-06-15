// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import serialize from 'form-serialize'

import {Row, Col, FormGroup, InputGroup, FormControl, Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import Switcher from 'react-switcher'

import ShareDialogPermission from './ShareDialogPermission'

import styles from './shareDialogContent.pcss'
import type {Permission} from '../../../flowtype/permission-types'
import {getResourcePermissions, addResourcePermission, removeResourcePermission} from '../../../actions/permission'

declare var _: any

class ShareDialogContent extends Component {
    form: HTMLFormElement
    onAnonymousAccessChange: Function
    onSubmit: Function
    props: {
        permissions: Array<Permission>,
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        anonymousPermission: ?Permission,
        owner: ?string,
        getResourcePermissions: () => {},
        addPermission: (permission: Permission) => {},
        removePermission: (permission: Permission) => {}
    }
    
    componentWillMount() {
        this.props.getResourcePermissions()
    }
    
    constructor() {
        super()
        
        this.onAnonymousAccessChange = this.onAnonymousAccessChange.bind(this)
        this.onSubmit = this.onSubmit.bind(this)
    }
    onAnonymousAccessChange() {
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
    onSubmit(e) {
        e.preventDefault()
        const data = serialize(this.form, {
            hash: true
        })
        this.props.addPermission({
            user: data.email,
            operation: 'read'
        })
        this.form.reset()
    }
    render() {
        return (
            <Row>
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
                <Row>
                    <Col xs={12} className={styles.permissionRow}>
                        {_.chain(this.props.permissions)
                            .groupBy(p => p.user)
                            .mapValues(permissions => (
                                <ShareDialogPermission
                                    resourceType={this.props.resourceType}
                                    resourceId={this.props.resourceId}
                                    key={`${permissions[0].user}`}
                                    permissions={permissions}
                                />
                            ))
                            .values()
                            .value()
                        }
                    </Col>
                </Row>
                <Col xs={12} className={styles.inputRow}>
                    <form onSubmit={this.onSubmit} ref={form => this.form = form}>
                        <FormGroup>
                            <InputGroup>
                                <FormControl type="email" placeholder="Enter email address" name="email" />
                                <InputGroup.Button>
                                    <Button className={styles.addButton} type="submit">
                                        <FontAwesome name="plus" />
                                    </Button>
                                </InputGroup.Button>
                            </InputGroup>
                        </FormGroup>
                    </form>
                </Col>
            </Row>
        )
    }
}

const mapStateToProps = ({permission: {byTypeAndId}}, ownProps) => {
    const byType = byTypeAndId[ownProps.resourceType] || {}
    const permissions = (byType[ownProps.resourceId] || []).filter(p => !p.removed)
    const ownerPermission = permissions.find(it => it.id === null && !it.new) || {}
    const owner = ownerPermission.user
    return {
        permissions: permissions.filter(p => !p.anonymous && (p.id || p.new)),
        anonymousPermission: permissions.find(p => p.anonymous),
        owner
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    getResourcePermissions() {
        dispatch(getResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    },
    addPermission(permission) {
        dispatch(addResourcePermission(ownProps.resourceType, ownProps.resourceId, permission))
    },
    removePermission(permission) {
        dispatch(removeResourcePermission(ownProps.resourceType, ownProps.resourceId, permission))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ShareDialogContent)
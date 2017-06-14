// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import serialize from 'form-serialize'

import {Row, Col, Button} from 'react-bootstrap'
import Switcher from 'react-switcher'

import ShareDialogPermission from './ShareDialogPermission'

import styles from './shareDialogContent.pcss'
import type {Permission} from '../../../flowtype/permission-types'
import {getResourcePermissions, addResourcePermission, removeResourcePermission} from '../../../actions/permission'

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
                {this.props.permissions.map(permission => (
                    <ShareDialogPermission key={`${permission.user}${permission.operation}`} permission={permission}/>
                ))}
                <Col xs={12}>
                    <form onSubmit={this.onSubmit} ref={form => this.form = form}>
                        <div className="input-group">
                            <input type="text" className="new-user-field form-control" placeholder="Enter email address" name="email"/>
                            <span className="input-group-btn">
                                <Button type="submit" className="new-user-button btn btn-default pull-right">
                                    <span className="icon fa fa-plus"/>
                                </Button>
                            </span>
                        </div>
                    </form>
                </Col>
            </Row>
        )
    }
}

const mapStateToProps = ({permission: {byTypeAndId}}, ownProps) => {
    const byType = byTypeAndId[ownProps.resourceType] || {}
    const permissions = (byType[ownProps.resourceId] || []).filter(p => !p.removed)
    const ownerPermission = permissions.find(it => it.id === null) || {}
    const owner = ownerPermission.user
    return {
        permissions: permissions.filter(p => p.id && !p.anonymous),
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
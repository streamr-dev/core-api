// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import serialize from 'form-serialize'

import {Row, Col, Button} from 'react-bootstrap'
import Switcher from 'react-switcher'

import ShareDialogPermission from './ShareDialogPermission'

import styles from './shareDialogContent.pcss'
import type {Permission} from '../../../flowtype/permission-types'
import {getResourcePermissions, addResourcePermission} from '../../../actions/permission'

class ShareDialogContent extends Component {
    form: HTMLFormElement
    onAnonymousAccessChange: Function
    onSubmit: Function
    props: {
        permissions: Array<Permission>,
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        anonymousAccess: boolean,
        owner: ?string,
        getResourcePermissions: () => {},
        addResourcePermission: (permission: Permission) => {}
    }
    
    static defaultProps = {
        anonymousAccess: false
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

    }
    onSubmit(e) {
        e.preventDefault()
        const data = serialize(this.form, {
            hash: true
        })
        this.props.addResourcePermission({
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
                        <Switcher on={this.props.anonymousAccess} onClick={this.onAnonymousAccessChange}/>
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
    const permissions = byType[ownProps.resourceId] || []
    const ownerPermission = permissions.find(it => it.id === null) || {}
    const owner = ownerPermission.user
    return {
        permissions: permissions.filter(p => p.id && !p.anonymous),
        anonymousAccess: permissions.find(p => p.anonymous),
        owner
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    getResourcePermissions() {
        dispatch(getResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    },
    addResourcePermission(permission) {
        dispatch(addResourcePermission(ownProps.resourceType, ownProps.resourceId, permission))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ShareDialogContent)
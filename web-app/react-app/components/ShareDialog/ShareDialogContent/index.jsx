// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {Row, Col, Button} from 'react-bootstrap'
import Switcher from 'react-switcher'

import ShareDialogPermission from './ShareDialogPermission'

import styles from './shareDialogContent.pcss'
import type {Permission} from '../../../flowtype/permission-types'
import {getResourcePermissions} from '../../../actions/permission'

class ShareDialogContent extends Component {
    input: HTMLInputElement
    onAnonymousAccessChange: Function
    props: {
        permissions: Array<Permission>,
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        anonymousAccess: boolean,
        dispatch: Function,
        owner: ?string
    }
    
    static defaultProps = {
        anonymousAccess: false
    }
    
    componentWillMount() {
        this.props.dispatch(getResourcePermissions(this.props.resourceType, this.props.resourceId))
    }
    
    constructor() {
        super()
        
        this.onAnonymousAccessChange = this.onAnonymousAccessChange.bind(this)
    }
    onAnonymousAccessChange() {
    
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
                    <ShareDialogPermission key={permission.id} permission={permission}/>
                ))}
                <Col xs={12}>
                    <div className="input-group">
                        <input type="text" className="new-user-field form-control"
                               placeholder="Enter email address" ref={i => this.input = i}/>
                        <span className="input-group-btn">
                            <Button className="new-user-button btn btn-default pull-right">
                                <span className="icon fa fa-plus"/>
                            </Button>
                        </span>
                    </div>
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
        permissions: permissions.filter(p => p.id !== null),
        owner
    }
}

export default connect(mapStateToProps)(ShareDialogContent)
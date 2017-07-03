// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Col, FormGroup, InputGroup, FormControl, Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import serialize from 'form-serialize'
import {addResourcePermission} from '../../../../actions/permission'

import styles from './shareDialogInputRow.pcss'

import type {Permission} from '../../../../flowtype/permission-types'

export class ShareDialogInputRow extends Component {
    form: HTMLFormElement
    onSubmit: Function
    
    props: {
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        addPermission: (permission: Permission) => {},
    }
    
    constructor() {
        super()
        this.onSubmit = this.onSubmit.bind(this)
    }
    
    onSubmit(e) {
        e.preventDefault()
        const data = serialize(e.target, {
            hash: true
        })
        this.props.addPermission({
            user: data.email,
            operation: 'read'
        })
        e.target.reset()
    }
    
    render() {
        return (
            <Col xs={12} className={styles.inputRow}>
                <form onSubmit={this.onSubmit}>
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
        )
    }
}

const mapStateToProps = (state) => ({})

const mapDispatchToProps = (dispatch, ownProps) => ({
    addPermission(permission) {
        dispatch(addResourcePermission(ownProps.resourceType, ownProps.resourceId, permission))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ShareDialogInputRow)
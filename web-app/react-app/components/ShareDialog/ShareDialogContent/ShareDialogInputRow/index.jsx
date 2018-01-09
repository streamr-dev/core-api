// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Col, FormGroup, InputGroup, FormControl, Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import serialize from 'form-serialize'
import {addResourcePermission} from '../../../../actions/permission'

import styles from './shareDialogInputRow.pcss'

import type {Permission} from '../../../../flowtype/permission-types'

type Props = {
    resourceType: Permission.resourceType,
    resourceId: Permission.resourceId,
    addPermission: (permission: Permission) => {},
    onClose: () => {}
}

export class ShareDialogInputRow extends Component<Props> {
    form: HTMLFormElement
    
    onSubmit = (e: {
        preventDefault: () => void,
        target: {
            reset: () => void
        }
    }) => {
        e.preventDefault()
        const data: {
            email: string
        } = serialize(e.target, {
            hash: true
        })
        if (data.email) {
            this.props.addPermission({
                user: data.email,
                operation: 'read'
            })
            e.target.reset()
        } else {
            this.props.onClose()
        }
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

export const mapDispatchToProps = (dispatch: Function, ownProps: Props) => ({
    addPermission(permission: Permission) {
        dispatch(addResourcePermission(ownProps.resourceType, ownProps.resourceId, permission))
    }
})

export default connect(null, mapDispatchToProps)(ShareDialogInputRow)
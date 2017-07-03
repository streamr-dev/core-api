// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import {Row, Modal} from 'react-bootstrap'

import ShareDialogInputRow from './ShareDialogInputRow'
import ShareDialogPermissionRow from './ShareDialogPermissionRow'
import ShareDialogOwnerRow from './ShareDialogOwnerRow'

import type {Permission} from '../../../flowtype/permission-types'
import {getResourcePermissions} from '../../../actions/permission'

export class ShareDialogContent extends Component {

    props: {
        resourceType: Permission.resourceType,
        resourceId: Permission.resourceId,
        getResourcePermissions: Function
    }
    
    componentWillMount() {
        this.props.getResourcePermissions()
    }
    
    render() {
        return (
            <Modal.Body>
                <Row>
                    <ShareDialogOwnerRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                    />
                    <Row>
                        <ShareDialogPermissionRow
                            resourceType={this.props.resourceType}
                            resourceId={this.props.resourceId}
                        />
                    </Row>
                    <ShareDialogInputRow
                        resourceType={this.props.resourceType}
                        resourceId={this.props.resourceId}
                    />
                </Row>
            </Modal.Body>
        )
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    getResourcePermissions() {
        dispatch(getResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect(null, mapDispatchToProps)(ShareDialogContent)
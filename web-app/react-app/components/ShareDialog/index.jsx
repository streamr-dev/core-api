// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Modal, Button} from 'react-bootstrap'
import ShareDialogContent from './ShareDialogContent'

import {saveUpdatedResourcePermissions} from '../../actions/permission'

import type {ReactChildren} from 'react-flow-types'
import type {Permission} from '../../flowtype/permission-types'

class ShareDialog extends Component {
    save: Function
    props: {
        resourceId: Permission.resourceId,
        resourceType: Permission.resourceType,
        resourceTitle: string,
        children?: ReactChildren,
        save: Function,
        isOpen: boolean,
        onClose: () => void
    }
    
    constructor() {
        super()
        this.save = this.save.bind(this)
    }
    
    save() {
        this.props.save()
            .then(() => {
                this.props.onClose()
            })
    }
    
    render() {
        return (
            <Modal
                show={this.props.isOpen}
                onHide={this.props.onClose}
                backdrop="static"
            >
                <Modal.Header closeButton>
                    <Modal.Title>Share {this.props.resourceTitle}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ShareDialogContent resourceTitle={this.props.resourceTitle} resourceType={this.props.resourceType} resourceId={this.props.resourceId} />
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        bsStyle="primary"
                        onClick={this.save}
                    >
                        Save
                    </Button>
                    <Button
                        bsStyle="default"
                        onClick={this.props.onClose}
                    >
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        )
    }
}

const mapStateToProps = ({permission}, ownProps) => ({
    permissions: permission.byTypeAndId[ownProps.resourceType] && permission.byTypeAndId[ownProps.resourceType][ownProps.resourceId] || []
})

const mapDispatchToProps = (dispatch, ownProps) => ({
    save() {
        return dispatch(saveUpdatedResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect (mapStateToProps, mapDispatchToProps)(ShareDialog)